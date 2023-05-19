package org.enodeframework.domain

import org.enodeframework.eventing.DomainEventMessage
import org.enodeframework.eventing.DomainEventStream

/**
 * Represents an aggregate root.
 */
interface AggregateRoot {
    /**
     * Represents the unique id of the aggregate root.
     */
    val uniqueId: String


    /**
     * Represents the current version of the aggregate root.
     */
    val version: Int


    /**
     * Get all the changes of the aggregate root.
     */
    val changes: List<DomainEventMessage>

    /**
     * Accept changes of the aggregate root.
     */
    fun acceptChanges()

    /**
     * Replay the given event streams.
     */
    fun replayEvents(eventStreams: List<DomainEventStream>)
}
