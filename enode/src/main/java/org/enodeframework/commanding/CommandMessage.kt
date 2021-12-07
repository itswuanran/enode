package org.enodeframework.commanding

import org.enodeframework.messaging.Message

/**
 * Represents a command.
 */
interface CommandMessage<T> : Message {
    /**
     * Represents the associated aggregate root string id.
     */
    var aggregateRootId: T

    fun getAggregateRootIdAsString(): String
}