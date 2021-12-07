package org.enodeframework.domain

import java.util.concurrent.CompletableFuture

interface AggregateRepository<T : AggregateRoot?> {
    fun getAsync(aggregateRootId: String): CompletableFuture<T>
}