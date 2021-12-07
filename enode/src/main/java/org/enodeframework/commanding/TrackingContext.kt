package org.enodeframework.commanding

import org.enodeframework.domain.AggregateRoot

interface TrackingContext {
    /**
     * Get all the tracked aggregates.
     */
    val trackedAggregateRoots: List<AggregateRoot>

    /**
     * Clear the tracking context.
     */
    fun clear()
}