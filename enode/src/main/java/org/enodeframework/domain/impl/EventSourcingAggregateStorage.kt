package org.enodeframework.domain.impl

import org.enodeframework.common.exception.AggregateRootInvalidException
import org.enodeframework.common.io.IOHelper
import org.enodeframework.common.utils.Assert
import org.enodeframework.domain.AggregateRoot
import org.enodeframework.domain.AggregateRootFactory
import org.enodeframework.domain.AggregateSnapshotter
import org.enodeframework.domain.AggregateStorage
import org.enodeframework.eventing.DomainEventStream
import org.enodeframework.eventing.EventStore
import org.enodeframework.infrastructure.TypeNameProvider
import java.util.concurrent.CompletableFuture

/**
 * @author anruence@gmail.com
 */
class EventSourcingAggregateStorage(
    private val eventStore: EventStore,
    private val aggregateRootFactory: AggregateRootFactory,
    private val aggregateSnapshotter: AggregateSnapshotter,
    private val typeNameProvider: TypeNameProvider
) : AggregateStorage {


    private fun <T : AggregateRoot?> tryRestoreFromSnapshotAsync(
        aggregateRootType: Class<T>, aggregateRootId: String, retryTimes: Int
    ): CompletableFuture<T> {
        val taskSource = CompletableFuture<T>()
        IOHelper.tryAsyncActionRecursively("TryRestoreFromSnapshotAsync", {
            aggregateSnapshotter.restoreFromSnapshotAsync(aggregateRootType, aggregateRootId)
        }, { value: T -> taskSource.complete(value) }, {
            String.format(
                "tryRestoreFromSnapshotAsync has unknown exception, aggregateRootType: %s, aggregateRootId: %s",
                aggregateRootType.name,
                aggregateRootId
            )
        }, null, retryTimes, true)
        return taskSource
    }

    private fun tryQueryAggregateEventsAsync(
        aggregateRootType: Class<*>,
        aggregateRootTypeName: String,
        aggregateRootId: String,
        minVersion: Int,
        maxVersion: Int,
        retryTimes: Int
    ): CompletableFuture<List<DomainEventStream>> {
        val taskSource = CompletableFuture<List<DomainEventStream>>()
        IOHelper.tryAsyncActionRecursively("TryQueryAggregateEventsAsync", {
            eventStore.queryAggregateEventsAsync(aggregateRootId, aggregateRootTypeName, minVersion, maxVersion)
        }, { value: List<DomainEventStream> -> taskSource.complete(value) }, {
            String.format(
                "eventStore.queryAggregateEventsAsync has unknown exception, aggregateRootTypeName: %s, aggregateRootId: %s",
                aggregateRootTypeName,
                aggregateRootId
            )
        }, null, retryTimes, true)
        return taskSource
    }

    private fun <T : AggregateRoot?> tryGetFromSnapshot(
        aggregateRootId: String, aggregateRootType: Class<T>
    ): CompletableFuture<T> {
        return tryRestoreFromSnapshotAsync(aggregateRootType, aggregateRootId, 0).thenCompose { aggregateRoot: T ->
            queryAggregateEventsSnapshot(aggregateRoot, aggregateRootId, aggregateRootType)
        }
    }

    private fun <T : AggregateRoot?> queryAggregateEventsSnapshot(
        aggregateRoot: T, aggregateRootId: String, aggregateRootType: Class<*>
    ): CompletableFuture<T> {
        if (aggregateRoot == null) {
            return CompletableFuture.completedFuture(null)
        }
        if (aggregateRoot.javaClass != aggregateRootType || aggregateRoot.uniqueId != aggregateRootId) {
            throw AggregateRootInvalidException(
                String.format(
                    "AggregateRoot recovery from snapshot is invalid as the aggregateRootType or aggregateRootId is not matched. Snapshot: [aggregateRootType: %s, aggregateRootId: %s], expected: [aggregateRootType: %s, aggregateRootId: %s]",
                    aggregateRoot.javaClass,
                    aggregateRoot.uniqueId,
                    aggregateRootType,
                    aggregateRootId
                )
            )
        }
        val aggregateRootTypeName = typeNameProvider.getTypeName(aggregateRootType)
        return tryQueryAggregateEventsAsync(
            aggregateRootType, aggregateRootTypeName, aggregateRootId, aggregateRoot.version + 1, MAX_VERSION, 0
        ).thenApply { eventStreams: List<DomainEventStream> ->
            aggregateRoot.replayEvents(eventStreams)
            aggregateRoot
        }
    }

    private fun <T : AggregateRoot?> rebuildAggregateRoot(
        aggregateRootType: Class<T>, eventStreams: List<DomainEventStream>
    ): T {
        return eventStreams.isNotEmpty().let {
            val aggregateRoot = aggregateRootFactory.createAggregateRoot(aggregateRootType)
            aggregateRoot?.replayEvents(eventStreams)
            aggregateRoot
        }
    }

    private val MIN_VERSION = 1
    private val MAX_VERSION = Int.MAX_VALUE

    override fun <T : AggregateRoot?> getAsync(
        aggregateRootType: Class<T>,
        aggregateRootId: String
    ): CompletableFuture<T> {
        Assert.nonNull(aggregateRootId, "aggregateRootId")
        Assert.nonNull(aggregateRootType, "aggregateRootType")
        val future = tryGetFromSnapshot(aggregateRootId, aggregateRootType)
        var result = CompletableFuture<T>()
        future.whenComplete { aggregateRoot, _ ->
            if (aggregateRoot != null) {
                result.complete(aggregateRoot)
            } else {
                result = queryAggregateEvents(aggregateRootType, aggregateRootId)
            }
        }
        return result
    }

    private fun <T : AggregateRoot?> queryAggregateEvents(
        aggregateRootType: Class<T>, aggregateRootId: String
    ): CompletableFuture<T> {
        val aggregateRootTypeName = typeNameProvider.getTypeName(aggregateRootType)
        return tryQueryAggregateEventsAsync(
            aggregateRootType, aggregateRootTypeName, aggregateRootId, MIN_VERSION, MAX_VERSION, 0
        ).thenApply { eventStreams ->
            rebuildAggregateRoot(aggregateRootType, eventStreams)
        }
    }
}