package org.enodeframework.domain

import java.util.concurrent.CompletableFuture

/**
 * Represents an aggregate storage interface.
 */
interface AggregateStorage {
    /**
     * Get an aggregate from aggregate storage.
     */
    fun <T : AggregateRoot?> getAsync(aggregateRootType: Class<T>, aggregateRootId: String): CompletableFuture<T>
}