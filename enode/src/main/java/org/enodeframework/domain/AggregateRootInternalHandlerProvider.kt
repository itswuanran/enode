package org.enodeframework.domain

import org.enodeframework.common.function.Action2
import org.enodeframework.eventing.DomainEventMessage

/**
 * Defines a provider interface to provide the aggregate root internal handler.
 */
interface AggregateRootInternalHandlerProvider {
    /**
     * Get the internal event handler within the aggregate.
     */
    fun getInternalEventHandler(
        aggregateRootType: Class<out AggregateRoot>,
        eventType: Class<out DomainEventMessage<*>>
    ): Action2<AggregateRoot, DomainEventMessage<*>>
}