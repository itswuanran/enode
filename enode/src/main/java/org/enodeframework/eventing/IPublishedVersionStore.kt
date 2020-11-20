package org.enodeframework.eventing

import java.util.concurrent.CompletableFuture

interface IPublishedVersionStore {
    /**
     * Update the published version for the given aggregate.
     */
    fun updatePublishedVersionAsync(processorName: String, aggregateRootTypeName: String, aggregateRootId: String, publishedVersion: Int): CompletableFuture<Int>

    /**
     * Get the current published version for the given aggregate.
     */
    fun getPublishedVersionAsync(processorName: String, aggregateRootTypeName: String, aggregateRootId: String): CompletableFuture<Int>
}