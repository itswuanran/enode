package org.enodeframework.eventing

/**
 * Represents an event.
 */
interface EventSerializer {
    /**
     * Serialize the given events to map.
     */
    fun serialize(evnts: List<DomainEventMessage<*>>): Map<String, String>

    /**
     * Deserialize the given data to events.
     */
    fun deserialize(data: Map<String, String>): List<DomainEventMessage<*>>
}