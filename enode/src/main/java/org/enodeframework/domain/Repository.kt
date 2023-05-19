package org.enodeframework.domain

import java.util.concurrent.CompletableFuture

interface Repository {
    /**
     * Get an aggregate from memory cache, if not exist, get it from event store.
     */
    fun <T : AggregateRoot?> getAsync(aggregateRootType: Class<T>, aggregateRootId: String): CompletableFuture<T>

    /**
     * Refresh memory cache with given aggregate.
     */
    fun <T : AggregateRoot?> refreshAggregate(aggregateRoot: T)
}
