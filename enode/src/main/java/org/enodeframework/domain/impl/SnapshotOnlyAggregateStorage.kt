package org.enodeframework.domain.impl

import org.enodeframework.common.exception.AggregateRootInvalidException
import org.enodeframework.common.io.IOHelper.tryAsyncActionRecursively
import org.enodeframework.common.utils.Assert
import org.enodeframework.domain.AggregateRoot
import org.enodeframework.domain.AggregateSnapshotter
import org.enodeframework.domain.AggregateStorage
import java.util.concurrent.CompletableFuture

/**
 * @author anruence@gmail.com
 */
class SnapshotOnlyAggregateStorage(private val aggregateSnapshotter: AggregateSnapshotter) : AggregateStorage {
    override fun <T : AggregateRoot?> getAsync(
        aggregateRootType: Class<T>,
        aggregateRootId: String
    ): CompletableFuture<T> {
        Assert.nonNull(aggregateRootId, "aggregateRootId")
        Assert.nonNull(aggregateRootType, "aggregateRootType")
        return tryRestoreFromSnapshotAsync(aggregateRootType, aggregateRootId, 0, CompletableFuture())
            .thenApply { aggregateRoot: T ->
                if (aggregateRoot != null && (aggregateRoot.javaClass != aggregateRootType || aggregateRoot.uniqueId != aggregateRootId)) {
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
                aggregateRoot
            }
    }

    private fun <T : AggregateRoot?> tryRestoreFromSnapshotAsync(
        aggregateRootType: Class<T>,
        aggregateRootId: String,
        retryTimes: Int,
        taskSource: CompletableFuture<T>
    ): CompletableFuture<T> {
        tryAsyncActionRecursively(
            "TryRestoreFromSnapshotAsync",
            { aggregateSnapshotter.restoreFromSnapshotAsync(aggregateRootType, aggregateRootId) },
            { value: T -> taskSource.complete(value) },
            {
                String.format(
                    "aggregateSnapshotter.tryRestoreFromSnapshotAsync has unknown exception, aggregateRootType: %s, aggregateRootId: %s",
                    aggregateRootType.name,
                    aggregateRootId
                )
            },
            null,
            retryTimes,
            true
        )
        return taskSource
    }
}
