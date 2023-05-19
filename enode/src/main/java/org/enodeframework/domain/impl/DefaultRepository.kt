package org.enodeframework.domain.impl

import org.enodeframework.common.utils.Assert
import org.enodeframework.domain.AggregateRoot
import org.enodeframework.domain.MemoryCache
import org.enodeframework.domain.Repository
import java.util.concurrent.CompletableFuture

/**
 * @author anruence@gmail.com
 */
class DefaultRepository(private val memoryCache: MemoryCache) : Repository {
    override fun <T : AggregateRoot?> getAsync(
        aggregateRootType: Class<T>,
        aggregateRootId: String
    ): CompletableFuture<T> {
        Assert.nonNull(aggregateRootType, "aggregateRootType")
        Assert.nonNull(aggregateRootId, "aggregateRootId")
        var result = CompletableFuture<T>()
        memoryCache.getAsync(aggregateRootId, aggregateRootType).whenComplete { aggregateRoot: T, _ ->
            if (aggregateRoot != null) {
                result.complete(aggregateRoot)
            } else {
                result = memoryCache.refreshAggregateFromEventStoreAsync(
                    aggregateRootType,
                    aggregateRootId
                )
            }
        }
        return result
    }

    override fun <T : AggregateRoot?> refreshAggregate(aggregateRoot: T) {
        memoryCache.refreshAggregate(aggregateRoot!!)
    }
}
