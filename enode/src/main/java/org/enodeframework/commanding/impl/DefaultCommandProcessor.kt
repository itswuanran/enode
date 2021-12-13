package org.enodeframework.commanding.impl

import kotlinx.coroutines.CoroutineDispatcher
import org.enodeframework.commanding.CommandProcessor
import org.enodeframework.commanding.ProcessingCommand
import org.enodeframework.commanding.ProcessingCommandHandler
import org.enodeframework.commanding.ProcessingCommandMailbox
import org.enodeframework.common.io.Task
import org.enodeframework.common.scheduling.ScheduleService
import org.enodeframework.common.utils.Assert
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * @author anruence@gmail.com
 */
class DefaultCommandProcessor(
    private val processingCommandHandler: ProcessingCommandHandler,
    private val scheduleService: ScheduleService,
    private val coroutineDispatcher: CoroutineDispatcher
) : CommandProcessor {
    private val mailboxDict: ConcurrentMap<String, ProcessingCommandMailbox>
    private val taskName: String
    private var aggregateRootMaxInactiveSeconds = 3600 * 24 * 3
    private var commandMailBoxPersistenceMaxBatchSize = 1000
    private var scanExpiredAggregateIntervalMilliseconds = 5000
    override fun process(processingCommand: ProcessingCommand) {
        val aggregateRootId = processingCommand.message.getAggregateRootIdAsString()
        Assert.nonNullOrEmpty(
            aggregateRootId,
            String.format("aggregateRootId of command, commandId: %s", processingCommand.message.id)
        )
        var mailbox = mailboxDict.computeIfAbsent(aggregateRootId) { x: String ->
            ProcessingCommandMailbox(
                x, processingCommandHandler, coroutineDispatcher, commandMailBoxPersistenceMaxBatchSize
            )
        }
        var mailboxTryUsingCount = 0L
        while (!mailbox.tryUsing()) {
            Task.sleep(1)
            mailboxTryUsingCount++
            if (mailboxTryUsingCount % 10000 == 0L) {
                logger.warn(
                    "Command mailbox try using count: {}, aggregateRootId: {}",
                    mailboxTryUsingCount,
                    mailbox.aggregateRootId
                )
            }
        }
        if (mailbox.isRemoved()) {
            mailbox = mailboxDict.computeIfAbsent(aggregateRootId) { x: String ->
                ProcessingCommandMailbox(
                    x, processingCommandHandler, coroutineDispatcher, commandMailBoxPersistenceMaxBatchSize
                )
            }
        }
        mailbox.enqueueMessage(processingCommand)
        mailbox.exitUsing()
    }

    override fun start() {
        scheduleService.startTask(
            taskName,
            { cleanInactiveMailbox() },
            scanExpiredAggregateIntervalMilliseconds,
            scanExpiredAggregateIntervalMilliseconds
        )
    }

    override fun stop() {
        scheduleService.stopTask(taskName)
    }

    private fun isMailBoxAllowRemove(mailbox: ProcessingCommandMailbox): Boolean {
        return mailbox.isInactive(aggregateRootMaxInactiveSeconds) && !mailbox.isRunning && mailbox.getTotalUnHandledMessageCount() == 0L
    }

    private fun cleanInactiveMailbox() {
        val inactiveList: List<Map.Entry<String, ProcessingCommandMailbox>> =
            mailboxDict.entries.filter { entry -> isMailBoxAllowRemove(entry.value) }
        inactiveList.forEach { entry: Map.Entry<String, ProcessingCommandMailbox> ->
            if (isMailBoxAllowRemove(entry.value)) {
                val removed = mailboxDict.remove(entry.key)
                if (removed != null) {
                    removed.markAsRemoved()
                    logger.info("Removed inactive command mailbox, aggregateRootId: {}", entry.key)
                }
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultCommandProcessor::class.java)
    }

    init {
        mailboxDict = ConcurrentHashMap()
        taskName = "CleanInactiveProcessingCommandMailBoxes_" + System.nanoTime() + Random().nextInt(10000)
    }
}