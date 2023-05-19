package org.enodeframework.domain

import java.util.concurrent.CompletableFuture

interface MemoryCache {
    /**
     * Get a strong type aggregate from memory cache.
     */
    fun <T : AggregateRoot?> getAsync(aggregateRootId: String, aggregateRootType: Class<T>): CompletableFuture<T>

    /**
     * Accept the given aggregate root's changes.
     */
    fun <T : AggregateRoot> acceptAggregateRootChanges(aggregateRoot: T)

    /**
     * Refresh the given aggregate.
     */
    fun <T : AggregateRoot> refreshAggregate(aggregateRoot: T)

    /**
     * Refresh the aggregate memory cache by replaying events of event store, and return the refreshed aggregate root.
     */
    fun <T : AggregateRoot?> refreshAggregateFromEventStoreAsync(
        aggregateRootTypeName: String, aggregateRootId: String
    ): CompletableFuture<T>

    /**
     * Refresh the aggregate memory cache by replaying events of event store, and return the refreshed aggregate root.
     */
    fun <T : AggregateRoot?> refreshAggregateFromEventStoreAsync(
        aggregateRootType: Class<T>, aggregateRootId: String
    ): CompletableFuture<T>

    /**
     * Start background tasks.
     */
    fun start()

    /**
     * Stop background tasks.
     */
    fun stop()
}