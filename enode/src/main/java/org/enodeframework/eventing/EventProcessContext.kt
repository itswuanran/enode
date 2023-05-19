package org.enodeframework.eventing

import java.util.concurrent.CompletableFuture

/**
 * Represents the event processing context.
 */
interface EventProcessContext {
    /**
     * Notify the event has been processed.
     */
    fun notifyEventProcessed(): CompletableFuture<Boolean>
}
