package org.enodeframework.domain.impl

import org.enodeframework.common.io.IOHelper.tryAsyncActionRecursively
import org.enodeframework.domain.AggregateRepositoryProvider
import org.enodeframework.domain.AggregateRepositoryProxy
import org.enodeframework.domain.AggregateRoot
import org.enodeframework.domain.AggregateSnapshotter
import java.util.concurrent.CompletableFuture

/**
 * @author anruence@gmail.com
 */
class DefaultAggregateSnapshotter(private val aggregateRepositoryProvider: AggregateRepositoryProvider) :
    AggregateSnapshotter {
    override fun <T : AggregateRoot?> restoreFromSnapshotAsync(
        aggregateRootType: Class<T>,
        aggregateRootId: String
    ): CompletableFuture<T> {
        val aggregateRepository = aggregateRepositoryProvider.getRepository(aggregateRootType)
        return tryGetAggregateAsync(aggregateRepository, aggregateRootType, aggregateRootId, 0)
    }

    private fun <T : AggregateRoot?> tryGetAggregateAsync(
        aggregateRepository: AggregateRepositoryProxy,
        aggregateRootType: Class<T>,
        aggregateRootId: String,
        retryTimes: Int
    ): CompletableFuture<T> {
        val taskSource = CompletableFuture<T>()
        tryAsyncActionRecursively(
            "TryGetAggregateAsync",
            { aggregateRepository.getAsync(aggregateRootId) },
            { result: T -> taskSource.complete(result) },
            {
                "aggregateRepository.getAsync has unknown exception, aggregateRepository: ${aggregateRepository.javaClass.name}, aggregateRootTypeName: ${aggregateRootType.name}, aggregateRootId: $aggregateRootId"
            },
            null,
            retryTimes,
            true
        )
        return taskSource
    }
}
