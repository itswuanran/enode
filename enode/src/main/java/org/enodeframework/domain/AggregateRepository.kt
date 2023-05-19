package org.enodeframework.domain

import java.util.concurrent.CompletableFuture

interface AggregateRepository<T : AggregateRoot?> {
    fun <T : AggregateRoot?> getAsync(aggregateRootId: String): CompletableFuture<T>
}