package org.enodeframework.commanding

import org.enodeframework.domain.AggregateRoot
import org.enodeframework.messaging.ApplicationMessage
import java.util.concurrent.CompletableFuture

interface CommandContext {
    /**
     * Add a new aggregate into the current command context.
     */
    suspend fun add(aggregateRoot: AggregateRoot)

    /**
     * Add a new aggregate into the current command context synchronously, and then return a completed task object.
     */
    fun addAsync(aggregateRoot: AggregateRoot): CompletableFuture<Boolean>

    /**
     * Get an aggregate sync from the current command context.
     */
    suspend fun <T : AggregateRoot?> get(id: String, firstFromCache: Boolean, aggregateRootType: Class<T>): T

    /**
     * Get an aggregate async from the current command context.
     */
    fun <T : AggregateRoot?> getAsync(
        id: String,
        firstFromCache: Boolean,
        aggregateRootType: Class<T>
    ): CompletableFuture<T>

    /**
     * Get an aggregate sync from the current command context, default from cache.
     */
    suspend fun <T : AggregateRoot?> get(id: String, aggregateRootType: Class<T>): T

    /**
     * Get an aggregate async from the current command context, default from cache.
     */
    fun <T : AggregateRoot?> getAsync(id: String, aggregateRootType: Class<T>): CompletableFuture<T>

    /**
     * Get result.
     */
    var result: String

    /**
     * Set an application message.
     */
    var applicationMessage: ApplicationMessage?
}