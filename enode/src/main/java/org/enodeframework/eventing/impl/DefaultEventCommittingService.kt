package org.enodeframework.eventing.impl

import kotlinx.coroutines.CoroutineDispatcher
import org.enodeframework.commanding.CommandResult
import org.enodeframework.commanding.CommandStatus
import org.enodeframework.commanding.ProcessingCommand
import org.enodeframework.common.io.IOHelper
import org.enodeframework.common.serializing.SerializeService
import org.enodeframework.domain.MemoryCache
import org.enodeframework.eventing.*
import org.enodeframework.messaging.MessagePublisher
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import kotlin.math.abs

/**
 * @author anruence@gmail.com
 */
class DefaultEventCommittingService(
    private val memoryCache: MemoryCache,
    private val eventStore: EventStore,
    private val serializeService: SerializeService,
    private val domainEventPublisher: MessagePublisher<DomainEventStream>,
    private val coroutineDispatcher: CoroutineDispatcher,
    private val eventMailBoxCount: Int
) : EventCommittingService {

    private val eventCommittingContextMailBoxList: MutableList<EventCommittingContextMailBox>

    constructor(
        memoryCache: MemoryCache,
        eventStore: EventStore,
        serializeService: SerializeService,
        domainEventPublisher: MessagePublisher<DomainEventStream>,
        coroutineDispatcher: CoroutineDispatcher
    ) : this(memoryCache, eventStore, serializeService, domainEventPublisher, coroutineDispatcher, 4)

    override fun commitDomainEventAsync(eventCommittingContext: EventCommittingContext) {
        val eventMailboxIndex = getEventMailBoxIndex(eventCommittingContext.eventStream.aggregateRootId)
        val eventMailbox = eventCommittingContextMailBoxList[eventMailboxIndex]
        eventMailbox.enqueueMessage(eventCommittingContext)
    }

    override fun publishDomainEventAsync(
        processingCommand: ProcessingCommand, eventStream: DomainEventStream
    ): CompletableFuture<Boolean> {
        // 这里取出event时，没有合并command的信息
        eventStream.mergeItems(processingCommand.items)
        val eventStreamMessage = DomainEventStream(
            processingCommand.message.id,
            eventStream.aggregateRootId,
            eventStream.version,
            eventStream.aggregateRootTypeName,
            eventStream.events,
            eventStream.items
        )
        return publishDomainEventAsync(processingCommand, eventStreamMessage, 0)
    }

    private fun getEventMailBoxIndex(aggregateRootId: String): Int {
        var hash = 23
        for (c in aggregateRootId.toCharArray()) {
            hash = (hash shl 5) - hash + c.toInt()
        }
        if (hash < 0) {
            hash = abs(hash)
        }
        return hash % eventMailBoxCount
    }

    private fun batchPersistEventAsync(committingContexts: List<EventCommittingContext>, retryTimes: Int) {
        if (committingContexts.isEmpty()) {
            return
        }
        IOHelper.tryAsyncActionRecursively("BatchPersistEventAsync", {
            eventStore.batchAppendAsync(committingContexts.map { obj: EventCommittingContext -> obj.eventStream })
        }, { result: EventAppendResult? ->
            val eventMailBox = committingContexts.first().mailBox
            if (result == null) {
                logger.error(
                    "Batch persist events success, but the persist result is null, the current event committing mailbox should be pending, mailboxNumber: {}",
                    eventMailBox.getNumber()
                )
                return@tryAsyncActionRecursively
            }
            val appendContextList = ArrayList<EventAppendContext>()
            //针对持久化成功的聚合根，正常发布这些聚合根的事件到Q端
            if (result.successAggregateRootIdList.size > 0) {
                for (aggregateRootId in result.successAggregateRootIdList) {
                    committingContexts.filter { x: EventCommittingContext -> x.eventStream.aggregateRootId == aggregateRootId }
                        .forEach { eventCommittingContext ->
                            val context = EventAppendContext()
                            context.success = true
                            context.duplicateCommandIdList = ArrayList()
                            context.committingContext = eventCommittingContext
                            appendContextList.add(context)
                        }
                }
                if (logger.isDebugEnabled) {
                    logger.debug(
                        "Batch persist events success, mailboxNumber: {}, result: {}",
                        eventMailBox.getNumber(),
                        serializeService.serialize(result.successAggregateRootIdList)
                    )
                }
            }
            //针对持久化出现重复的命令ID，在命令MailBox中标记为已重复，在事件MailBox中清除对应聚合根产生的事件，且重新发布这些命令对应的领域事件到Q端
            if (result.duplicateCommandAggregateRootIdList.isNotEmpty()) {
                for ((key, value) in result.duplicateCommandAggregateRootIdList) {
                    committingContexts.filter { x: EventCommittingContext -> key == x.eventStream.aggregateRootId }
                        .firstOrNull { eventCommittingContext: EventCommittingContext ->
                            val context = EventAppendContext()
                            context.duplicateCommandIdList = value
                            context.committingContext = eventCommittingContext
                            appendContextList.add(context)
                        }
                }
                logger.warn(
                    "Batch persist events has duplicate commandIds, mailboxNumber: {}, result: {}",
                    eventMailBox.getNumber(),
                    serializeService.serialize(result.duplicateCommandAggregateRootIdList)
                )
            }
            //针对持久化出现版本冲突的聚合根，则自动处理每个聚合根的冲突
            if (result.duplicateEventAggregateRootIdList.size > 0) {
                for (aggregateRootId in result.duplicateEventAggregateRootIdList) {
                    committingContexts.filter { x: EventCommittingContext -> x.eventStream.aggregateRootId == aggregateRootId }
                        .firstOrNull { eventCommittingContext ->
                            val context = EventAppendContext()
                            context.duplicateCommandIdList = ArrayList()
                            context.committingContext = eventCommittingContext
                            appendContextList.add(context)
                        }
                }
                logger.warn(
                    "Batch persist events duplicated, mailboxNumber: {}, result: {}",
                    eventMailBox.getNumber(),
                    serializeService.serialize(result.duplicateEventAggregateRootIdList)
                )
            }
            processDuplicateAggregateRootRecursively(0, appendContextList, eventMailBox)
            //最终将当前的EventMailBox的本次处理标记为处理完成，然后继续可以处理下一批事件
        }, {
            String.format("[contextListCount:%d]", committingContexts.size)
        }, null, retryTimes, true)
    }

    class EventAppendContext {
        lateinit var committingContext: EventCommittingContext
        var duplicateCommandIdList: List<String> = ArrayList()
        var success: Boolean = false
    }

    private fun processDuplicateAggregateRootRecursively(
        index: Int, contexts: List<EventAppendContext>, eventMailBox: EventCommittingContextMailBox
    ) {
        if (contexts.isEmpty()) {
            return
        }
        if (index == contexts.size) {
            eventMailBox.completeRun()
            return
        }
        val context = contexts[index]
        val eventCommittingContext = context.committingContext
        val duplicateCommandIdList = context.duplicateCommandIdList
        if (context.success) {
            publishDomainEventAsync(
                eventCommittingContext.processingCommand, eventCommittingContext.eventStream
            ).whenComplete { _, _ ->
                processDuplicateAggregateRootRecursively(index + 1, contexts, eventMailBox)
            }
            return
        }
        if (eventCommittingContext.eventStream.version == 1) {
            handleFirstEventDuplicationAsync(eventCommittingContext, 0).whenComplete { _, _ ->
                processDuplicateAggregateRootRecursively(index + 1, contexts, eventMailBox)
            }
        } else {
            resetCommandMailBoxConsumingSequence(
                eventCommittingContext, eventCommittingContext.processingCommand.sequence, duplicateCommandIdList
            ).whenComplete { _, _ ->
                processDuplicateAggregateRootRecursively(index + 1, contexts, eventMailBox)
            }
        }
    }

    private fun resetCommandMailBoxConsumingSequence(
        context: EventCommittingContext, consumingSequence: Long, duplicateCommandIdList: List<String>?
    ): CompletableFuture<Boolean> {
        val commandMailBox = context.processingCommand.mailBox
        val eventMailBox = context.mailBox
        val aggregateRootId = context.eventStream.aggregateRootId
        commandMailBox.pause()
        val future = CompletableFuture<Boolean>()
        eventMailBox.removeAggregateAllEventCommittingContexts(aggregateRootId)
        memoryCache.refreshAggregateFromEventStoreAsync(context.eventStream.aggregateRootTypeName, aggregateRootId)
            .whenComplete { _, _ ->
                try {
                    if (duplicateCommandIdList != null) {
                        for (commandId in duplicateCommandIdList) {
                            commandMailBox.addDuplicateCommandId(commandId)
                        }
                    }
                    commandMailBox.resetConsumingSequence(consumingSequence)
                } finally {
                    commandMailBox.resume()
                    commandMailBox.tryRun()
                }
                future.complete(true)
            }
        return future
    }

    private fun handleFirstEventDuplicationAsync(
        context: EventCommittingContext, retryTimes: Int
    ): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        IOHelper.tryAsyncActionRecursively("FindFirstEventByVersion", {
            eventStore.findAsync(context.eventStream.aggregateRootId, 1)
        }, { result: DomainEventStream? ->
            if (result != null) {
                //判断是否是同一个command，如果是，则再重新做一遍发布事件；
                //之所以要这样做，是因为虽然该command产生的事件已经持久化成功，但并不表示事件也已经发布出去了；
                //有可能事件持久化成功了，但那时正好机器断电了，则发布事件都没有做；
                if (context.processingCommand.message.id == result.commandId) {
                    resetCommandMailBoxConsumingSequence(
                        context, context.processingCommand.sequence + 1, null
                    ).whenComplete { _, _ ->
                        publishDomainEventAsync(context.processingCommand, result).whenComplete { _, _ ->
                            future.complete(true)
                        }
                    }
                } else {
                    //如果不是同一个command，则认为是两个不同的command重复创建ID相同的聚合根，我们需要记录错误日志，然后通知当前command的处理完成；
                    val errorMessage = String.format(
                        "Duplicate aggregate creation. current commandId:%s, existing commandId:%s, aggregateRootId:%s, aggregateRootTypeName:%s",
                        context.processingCommand.message.id,
                        result.commandId,
                        result.aggregateRootId,
                        result.aggregateRootTypeName
                    )
                    logger.error(errorMessage)
                    resetCommandMailBoxConsumingSequence(
                        context, context.processingCommand.sequence + 1, null
                    ).whenComplete { _, _ ->
                        val commandResult = CommandResult(
                            CommandStatus.Failed,
                            context.processingCommand.message.id,
                            context.eventStream.aggregateRootId,
                            "Duplicate aggregate creation.",
                            String::class.java.name
                        )
                        completeCommand(context.processingCommand, commandResult).whenComplete { _, _ ->
                            future.complete(true)
                        }
                    }
                }
            } else {
                val errorMessage = String.format(
                    "Duplicate aggregate creation, but we cannot find the existing eventstream from eventstore. commandId:%s, aggregateRootId:%s, aggregateRootTypeName:%s",
                    context.eventStream.commandId,
                    context.eventStream.aggregateRootId,
                    context.eventStream.aggregateRootTypeName
                )
                logger.error(errorMessage)
                resetCommandMailBoxConsumingSequence(
                    context, context.processingCommand.sequence + 1, null
                ).whenComplete { _, _ ->
                    val commandResult = CommandResult(
                        CommandStatus.Failed,
                        context.processingCommand.message.id,
                        context.eventStream.aggregateRootId,
                        "Duplicate aggregate creation, but we cannot find the existing eventstream from eventstore.",
                        String::class.java.name
                    )
                    completeCommand(context.processingCommand, commandResult).whenComplete { _, _ ->
                        future.complete(
                            true
                        )
                    }
                }
            }
        }, {
            String.format("[eventStream:%s]", serializeService.serialize(context.eventStream))
        }, null, retryTimes, true)
        return future
    }

    private fun publishDomainEventAsync(
        processingCommand: ProcessingCommand, eventStream: DomainEventStream, retryTimes: Int
    ): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        IOHelper.tryAsyncActionRecursivelyWithoutResult("PublishDomainEventAsync", {
            domainEventPublisher.publishAsync(eventStream)
        }, {
            if (logger.isDebugEnabled) {
                logger.debug("Publish domain events success, {}", serializeService.serialize(eventStream))
            }
            val commandHandleResult = processingCommand.commandExecuteContext.result
            val commandResult = CommandResult(
                CommandStatus.Success,
                processingCommand.message.id,
                eventStream.aggregateRootId,
                commandHandleResult,
                String::class.java.name
            )
            completeCommand(processingCommand, commandResult).whenComplete { _, _ ->
                future.complete(true)
            }
        }, {
            String.format("[eventStream:%s]", serializeService.serialize(eventStream))
        }, null, retryTimes, true)
        return future
    }

    private fun completeCommand(
        processingCommand: ProcessingCommand, commandResult: CommandResult
    ): CompletableFuture<Boolean> {
        return processingCommand.mailBox.completeMessage(processingCommand, commandResult)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultEventCommittingService::class.java)
    }

    init {
        eventCommittingContextMailBoxList = ArrayList()
        for (i in 0 until eventMailBoxCount) {
            val mailBox =
                EventCommittingContextMailBox(i, 1000, coroutineDispatcher) { x: List<EventCommittingContext> ->
                    batchPersistEventAsync(
                        x, 0
                    )
                }
            eventCommittingContextMailBoxList.add(mailBox)
        }
    }
}