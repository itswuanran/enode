package org.enodeframework.queue.command

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.cache.RemovalCause
import org.enodeframework.commanding.CommandMessage
import org.enodeframework.commanding.CommandResult
import org.enodeframework.commanding.CommandReturnType
import org.enodeframework.commanding.CommandStatus
import org.enodeframework.common.exception.DuplicateCommandRegisterException
import org.enodeframework.common.extensions.SystemClock
import org.enodeframework.common.scheduling.ScheduleService
import org.enodeframework.common.scheduling.Worker
import org.enodeframework.common.serializing.SerializeService
import org.enodeframework.queue.reply.GenericReplyMessage
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * @author anruence@gmail.com
 */
class DefaultCommandResultProcessor(
    private val scheduleService: ScheduleService,
    private val serializeService: SerializeService,
    private val uniqueAddress: String,
    private val completionSourceTimeout: Int
) : CommandResultProcessor {
    private val scanExpireCommandTaskName: String =
        "CleanTimeoutCommandTask_" + SystemClock.now() + Random().nextInt(5000)
    private val commandTaskDict: Cache<String, CommandTaskCompletionSource>
    private val commandExecutedMessageLocalQueue: BlockingQueue<GenericReplyMessage>
    private val commandExecutedMessageWorker: Worker

    override fun registerProcessingCommand(
        command: CommandMessage,
        commandReturnType: CommandReturnType,
        taskCompletionSource: CompletableFuture<CommandResult>
    ) {
        if (commandTaskDict.asMap().putIfAbsent(
                command.id, CommandTaskCompletionSource(
                    command.aggregateRootId, commandReturnType, taskCompletionSource
                )
            ) != null
        ) {
            throw DuplicateCommandRegisterException(
                "Duplicate processing command registration, type:${command.javaClass.name}, id:${command.id}"
            )
        }
    }

    fun start() {
        commandExecutedMessageWorker.start()
        scheduleService.startTask(
            scanExpireCommandTaskName, { commandTaskDict.cleanUp() }, completionSourceTimeout, completionSourceTimeout
        )
    }

    fun stop() {
        scheduleService.stopTask(scanExpireCommandTaskName)
        commandExecutedMessageWorker.stop()
    }

    override fun uniqueReplyAddress(): String {
        return uniqueAddress
    }

    override fun processReplyMessage(replyMessage: GenericReplyMessage) {
        if (replyMessage.returnType == CommandReturnType.CommandExecuted.value
            || replyMessage.returnType == CommandReturnType.EventHandled.value
        ) {
            commandExecutedMessageLocalQueue.add(replyMessage)
        }
    }

    private fun processCommandReturnMessage(replyMessage: GenericReplyMessage) {
        if (replyMessage.returnType == CommandReturnType.EventHandled.value) {
            return processDomainEventHandledMessage(replyMessage)
        }
        return processExecutedCommandMessage(replyMessage)
    }

    private fun processExecutedCommandMessage(replyMessage: GenericReplyMessage) {
        val commandTaskCompletionSource = commandTaskDict.asMap()[replyMessage.commandId]
        if (commandTaskCompletionSource == null) {
            if (logger.isDebugEnabled) {
                logger.debug(
                    "Command result return, {}, but commandTaskCompletionSource maybe timeout expired.", replyMessage
                )
            }
            return
        }
        if (commandTaskCompletionSource.commandReturnType == CommandReturnType.CommandExecuted) {
            commandTaskDict.asMap().remove(replyMessage.commandId)
            if (commandTaskCompletionSource.taskCompletionSource.complete(replyMessage.asCommandResult())) {
                if (logger.isDebugEnabled) {
                    logger.debug("Command result return CommandExecuted, {}", serializeService.serialize(replyMessage))
                }
            }
        } else if (commandTaskCompletionSource.commandReturnType == CommandReturnType.EventHandled) {
            if (CommandStatus.Success.value != replyMessage.status) {
                commandTaskDict.asMap().remove(replyMessage.commandId)
                if (commandTaskCompletionSource.taskCompletionSource.complete(replyMessage.asCommandResult())) {
                    if (logger.isDebugEnabled) {
                        logger.debug(
                            "Command result return EventHandled, {}", serializeService.serialize(replyMessage)
                        )
                    }
                }
            }
        }
    }

    private fun processTimeoutCommand(commandId: String, commandTaskCompletionSource: CommandTaskCompletionSource?) {
        if (commandTaskCompletionSource != null) {
            logger.error("Wait command notify timeout, commandId: {}", commandId)
            val commandResult = CommandResult(
                CommandStatus.Failed,
                commandId,
                commandTaskCompletionSource.aggregateRootId,
                "Wait command notify timeout.",
            )
            // 任务超时失败
            commandTaskCompletionSource.taskCompletionSource.complete(commandResult)
        }
    }

    private fun processDomainEventHandledMessage(message: GenericReplyMessage) {
        val commandTaskCompletionSource = commandTaskDict.asMap()[message.commandId]
        if (commandTaskCompletionSource != null) {
            if (CommandReturnType.EventHandled != commandTaskCompletionSource.commandReturnType) {
                logger.warn("event arrived early than command: {}", serializeService.serialize(message))
                return
            }
            commandTaskDict.asMap().remove(message.commandId)
            commandTaskCompletionSource.taskCompletionSource.complete(message.asCommandResult())
            if (logger.isDebugEnabled) {
                logger.debug("DomainEvent result return, {}", serializeService.serialize(message))
            }
        }
    }

    private val logger = LoggerFactory.getLogger(DefaultCommandResultProcessor::class.java)

    init {
        commandTaskDict = CacheBuilder.newBuilder().removalListener { notification ->
            if (notification.cause == RemovalCause.EXPIRED) {
                processTimeoutCommand(notification.key!!, notification.value)
            }
        }.expireAfterWrite(completionSourceTimeout.toLong(), TimeUnit.MILLISECONDS).build()
        commandExecutedMessageLocalQueue = LinkedBlockingQueue()
        commandExecutedMessageWorker = Worker("ProcessExecutedCommandMessage") {
            processCommandReturnMessage(
                commandExecutedMessageLocalQueue.take()
            )
        }
    }
}