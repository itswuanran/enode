package org.enodeframework.commanding.impl

import com.google.common.base.Strings
import kotlinx.coroutines.future.await
import org.enodeframework.commanding.CommandExecuteContext
import org.enodeframework.commanding.CommandResult
import org.enodeframework.common.exception.AggregateRootAlreadyExistException
import org.enodeframework.common.exception.AggregateRootNotFoundException
import org.enodeframework.common.io.Task
import org.enodeframework.common.utils.Assert
import org.enodeframework.domain.AggregateRoot
import org.enodeframework.domain.AggregateStorage
import org.enodeframework.domain.Repository
import org.enodeframework.messaging.ApplicationMessage
import org.enodeframework.queue.MessageContext
import org.enodeframework.queue.QueueMessage
import org.enodeframework.queue.SendReplyService
import org.enodeframework.queue.command.GenericCommandMessage
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * @author anruence@gmail.com
 */
class DefaultCommandExecuteContext(
    repository: Repository,
    aggregateRootStorage: AggregateStorage,
    queueMessage: QueueMessage,
    messageContext: MessageContext,
    genericCommandMessage: GenericCommandMessage,
    sendReplyService: SendReplyService
) : CommandExecuteContext {
    private val trackingAggregateRootDict: ConcurrentMap<String, AggregateRoot>
    private val repository: Repository
    private val aggregateRootStorage: AggregateStorage
    private val sendReplyService: SendReplyService
    private val queueMessage: QueueMessage
    private val messageContext: MessageContext
    private val genericCommandMessage: GenericCommandMessage
    override var result: String = ""
    override var applicationMessage: ApplicationMessage? = null
    override fun onCommandExecutedAsync(commandResult: CommandResult): CompletableFuture<Boolean> {
        messageContext.onMessageHandled(queueMessage)
        return if (Strings.isNullOrEmpty(genericCommandMessage.replyAddress)) {
            Task.completedTask
        } else sendReplyService.sendCommandReply(commandResult, genericCommandMessage.replyAddress)
    }

    override suspend fun add(aggregateRoot: AggregateRoot) {
        addAsync(aggregateRoot).await()
    }

    private fun addInternal(aggregateRoot: AggregateRoot) {
        Assert.nonNull(aggregateRoot, "aggregateRoot")
        if (trackingAggregateRootDict.containsKey(aggregateRoot.uniqueId)) {
            throw AggregateRootAlreadyExistException(aggregateRoot.uniqueId, aggregateRoot.javaClass)
        }
        trackingAggregateRootDict[aggregateRoot.uniqueId] = aggregateRoot
    }

    override fun addAsync(aggregateRoot: AggregateRoot): CompletableFuture<Boolean> {
        addInternal(aggregateRoot)
        return Task.completedTask
    }

    /**
     * Get an aggregate from the current command context.
     */
    override fun <T : AggregateRoot> getAsync(
        id: Any,
        firstFromCache: Boolean,
        aggregateRootType: Class<T>
    ): CompletableFuture<T> {
        Assert.nonNull(id, "id")
        val aggregateRootId = id.toString()
        val iAggregateRoot = trackingAggregateRootDict[aggregateRootId] as T?
        var future = CompletableFuture<T>()
        if (iAggregateRoot != null) {
            future.complete(iAggregateRoot)
            return future
        }
        future = if (firstFromCache) {
            repository.getAsync(aggregateRootType, id)
        } else {
            aggregateRootStorage.getAsync(aggregateRootType, aggregateRootId)
        }
        return future.thenApply { aggregateRoot: T? ->
            if (aggregateRoot == null) {
                throw AggregateRootNotFoundException(aggregateRootId, aggregateRootType)
            }
            trackingAggregateRootDict[aggregateRoot.uniqueId] = aggregateRoot
            repository.refreshAggregate(aggregateRoot)
            aggregateRoot
        }
    }

    override fun <T : AggregateRoot> getAsync(id: Any, aggregateRootType: Class<T>): CompletableFuture<T> {
        return getAsync(id, true, aggregateRootType)
    }

    override suspend fun <T : AggregateRoot> get(id: Any, firstFromCache: Boolean, aggregateRootType: Class<T>): T {
        return getAsync(id, firstFromCache, aggregateRootType).await()
    }

    override suspend fun <T : AggregateRoot> get(id: Any, aggregateRootType: Class<T>): T {
        return getAsync(id, aggregateRootType).await()
    }

    override val trackedAggregateRoots: List<AggregateRoot>
        get() = ArrayList(trackingAggregateRootDict.values)

    override fun clear() {
        trackingAggregateRootDict.clear()
        result = ""
    }

    init {
        trackingAggregateRootDict = ConcurrentHashMap()
        this.repository = repository
        this.aggregateRootStorage = aggregateRootStorage
        this.sendReplyService = sendReplyService
        this.queueMessage = queueMessage
        this.genericCommandMessage = genericCommandMessage
        this.messageContext = messageContext
    }
}