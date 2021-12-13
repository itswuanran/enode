package org.enodeframework.commanding

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import org.enodeframework.common.extensions.SystemClock
import org.enodeframework.common.io.Task
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * @author anruence@gmail.com
 */
class ProcessingCommandMailbox(
    var aggregateRootId: String,
    private val messageHandler: ProcessingCommandHandler,
    private val coroutineDispatcher: CoroutineDispatcher,
    private val batchSize: Int
) {
    private val lockObj = Any()
    private val asyncLockObj = Any()
    private var messageDict: ConcurrentHashMap<Long, ProcessingCommand> = ConcurrentHashMap()
    private var duplicateCommandIdDict: ConcurrentHashMap<String, Byte> = ConcurrentHashMap()
    private val isUsing = AtomicInteger(0)
    private val isRemoved = AtomicInteger(0)
    private var lastActiveTime: Date
    var isRunning = false
        private set
    private var isPauseRequested = false
    var isPaused = false
        private set
    private var nextSequence: Long = 0
    var consumingSequence: AtomicLong = AtomicLong(0L)
        private set

    fun getTotalUnHandledMessageCount(): Long {
        return nextSequence - consumingSequence.get()
    }

    /**
     * 放入一个消息到MailBox，并自动尝试运行MailBox
     */
    fun enqueueMessage(message: ProcessingCommand) {
        synchronized(lockObj) {
            message.sequence = nextSequence
            message.mailBox = this
            // If the specified key is not already associated with a value (or is mapped to null) associates it with the given value and returns null, else returns the current value.
            if (messageDict.putIfAbsent(message.sequence, message) == null) {
                nextSequence++
                if (logger.isDebugEnabled) {
                    logger.debug(
                        "{} enqueued new message, aggregateRootId: {}, messageSequence: {}",
                        javaClass.name,
                        aggregateRootId,
                        message.sequence
                    )
                }
                lastActiveTime = Date()
                tryRun()
            } else {
                logger.error(
                    "{} enqueue message failed, aggregateRootId: {}, messageId: {}, messageSequence: {}",
                    javaClass.name,
                    aggregateRootId,
                    message.message.id,
                    message.sequence
                )
            }
        }
    }

    fun tryRun() {
        synchronized(lockObj) {
            if (isRunning || isPauseRequested || isPaused) {
                return
            }
            setAsRunning()
            if (logger.isDebugEnabled) {
                logger.debug(
                    "{} start run, aggregateRootId: {}, consumingSequence: {}",
                    javaClass.name,
                    aggregateRootId,
                    consumingSequence.get()
                )
            }
            CoroutineScope(coroutineDispatcher).async { processMessages() }
            return
        }
    }

    /**
     * 请求完成MailBox的单次运行，如果MailBox中还有剩余消息，则继续尝试运行下一次
     */
    private fun completeRun() {
        lastActiveTime = Date()
        if (logger.isDebugEnabled) {
            logger.debug("{} complete run, aggregateRootId: {}", javaClass.name, aggregateRootId)
        }
        setAsNotRunning()
        if (getTotalUnHandledMessageCount() > 0) {
            tryRun()
        }
    }

    /**
     * 暂停当前MailBox的运行，暂停成功可以确保当前MailBox不会处于运行状态，也就是不会在处理任何消息
     */
    fun pause() {
        isPauseRequested = true
        if (logger.isDebugEnabled) {
            logger.debug("{} pause requested, aggregateRootId: {}", javaClass.name, aggregateRootId)
        }
        var count = 0L
        while (isRunning) {
            Task.sleep(10)
            count++
            if (count % 100 == 0L) {
                if (logger.isDebugEnabled) {
                    logger.debug(
                        "{} pause requested, but wait for too long to stop the current mailbox, aggregateRootId: {}, waitCount: {}",
                        javaClass.name,
                        aggregateRootId,
                        count
                    )
                }
            }
        }
        lastActiveTime = Date()
        isPaused = true
    }

    /**
     * 恢复当前MailBox的运行，恢复后，当前MailBox又可以进行运行，需要手动调用TryRun方法来运行
     */
    fun resume() {
        isPauseRequested = false
        isPaused = false
        lastActiveTime = Date()
        if (logger.isDebugEnabled) {
            logger.debug(
                "{} resume requested, aggregateRootId: {}, consumingSequence: {}",
                javaClass.name,
                aggregateRootId,
                consumingSequence.get()
            )
        }
    }

    fun addDuplicateCommandId(commandId: String) {
        duplicateCommandIdDict.putIfAbsent(commandId, 1.toByte())
    }

    fun resetConsumingSequence(consumingSequence: Long) {
        this.consumingSequence.set(consumingSequence)
        lastActiveTime = Date()
        if (logger.isDebugEnabled) {
            logger.debug(
                "{} reset consumingSequence, aggregateRootId: {}, consumingSequence: {}",
                javaClass.name,
                aggregateRootId,
                consumingSequence
            )
        }
    }

    fun completeMessage(message: ProcessingCommand, result: CommandResult): CompletableFuture<Boolean> {
        try {
            val removed = messageDict.remove(message.sequence)
            if (removed != null) {
                duplicateCommandIdDict.remove(message.message.id)
                lastActiveTime = Date()
                return message.completeAsync(result)
            }
        } catch (ex: Exception) {
            logger.error(
                "{} complete message with result failed, aggregateRootId: {}, messageId: {}, messageSequence: {}, result: {}",
                javaClass.name,
                aggregateRootId,
                message.message.id,
                message.sequence,
                result,
                ex
            )
        }
        return Task.completedTask
    }

    fun isInactive(timeoutSeconds: Int): Boolean {
        return SystemClock.now() - lastActiveTime.time >= timeoutSeconds
    }

    private fun processMessages() {
        synchronized(asyncLockObj) {
            lastActiveTime = Date()
            try {
                processMessagesRecursively(getTotalUnHandledMessageCount(), 0)
            } catch (ex: Exception) {
                logger.error("{} run has unknown exception, aggregateRootId: {}", javaClass.name, aggregateRootId, ex)
                Task.sleep(1)
                completeRun()
            }
        }
    }

    /**
     * 处理消息的递归实现
     */
    private fun processMessagesRecursively(unHandledMessageCount: Long, scannedCount: Long) {
        if (!(unHandledMessageCount > 0 && scannedCount < batchSize && !isPauseRequested)) {
            completeRun()
            return
        }
        val message = getMessage(consumingSequence.get())
        if (message != null) {
            if (duplicateCommandIdDict.containsKey(message.message.id)) {
                message.isDuplicated = true
            }
            messageHandler.handleAsync(message).whenComplete { _, _ ->
                consumingSequence.incrementAndGet()
                processMessagesRecursively(getTotalUnHandledMessageCount(), scannedCount + 1)
            }
        } else {
            consumingSequence.incrementAndGet()
            processMessagesRecursively(getTotalUnHandledMessageCount(), scannedCount + 1)
        }
    }


    private fun getMessage(sequence: Long): ProcessingCommand? {
        return messageDict[sequence]
    }

    private fun setAsRunning() {
        isRunning = true
    }

    fun tryUsing(): Boolean {
        return isUsing.compareAndSet(0, 1)
    }

    fun exitUsing() {
        isUsing.set(0)
    }

    fun markAsRemoved() {
        isRemoved.set(1)
    }

    fun isRemoved(): Boolean {
        return isRemoved.get() == 1
    }

    private fun setAsNotRunning() {
        isRunning = false
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ProcessingCommandMailbox::class.java)
    }

    init {
        lastActiveTime = Date()
    }
}