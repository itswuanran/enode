package org.enodeframework.domain

import org.enodeframework.infrastructure.ObjectProxy
import java.util.concurrent.CompletableFuture

interface AggregateRepositoryProxy : ObjectProxy {
    fun <T : AggregateRoot?> getAsync(aggregateRootId: String): CompletableFuture<T>
}