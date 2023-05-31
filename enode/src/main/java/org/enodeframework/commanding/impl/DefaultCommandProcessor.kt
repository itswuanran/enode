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
    private val coroutineDispatcher: CoroutineDispatcher,
    /**
     * 当使用默认的从内存清理聚合根的服务时，该属性用于配置扫描过期的聚合根的时间间隔，默认为5秒；
     */
    private val scanExpiredAggregateIntervalMilliseconds: Int = 5000,
    /**
     * 当使用默认的MemoryCache时，该属性用于配置聚合根的最长允许的不活跃时间，超过这个时间就认为是过期，就可以从内存清除了；然后下次如果再需要用的时候再重新加载进来；默认为3天；
     */
    private val aggregateRootMaxInactiveSeconds: Int = 3600 * 24 * 3,
    /**
     * CommandMailBox中的命令处理时一次最多处理多少个命令，默认为1000个
     */
    private val commandMailBoxPersistenceMaxBatchSize: Int = 1000
) : CommandProcessor {
    constructor(
        processingCommandHandler: ProcessingCommandHandler,
        scheduleService: ScheduleService,
        coroutineDispatcher: CoroutineDispatcher
    ) : this(
        processingCommandHandler, scheduleService, coroutineDispatcher, 5000, 3600 * 24 * 3, 1000
    )

    private val logger = LoggerFactory.getLogger(DefaultCommandProcessor::class.java)
    private val mailboxDict: ConcurrentMap<String, ProcessingCommandMailbox>
    private val taskName: String
    override fun process(processingCommand: ProcessingCommand) {
        val aggregateRootId = processingCommand.message.aggregateRootId
        Assert.nonNullOrEmpty(
            aggregateRootId,
            "aggregateRootId of command, commandId: ${processingCommand.message.id}"
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


    init {
        mailboxDict = ConcurrentHashMap()
        taskName = "CleanInactiveProcessingCommandMailBoxes_" + System.nanoTime() + Random().nextInt(10000)
    }
}