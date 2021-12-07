package org.enodeframework.domain

/**
 * Defines a factory to create empty aggregate root.
 */
interface AggregateRootFactory {
    /**
     * Create an empty aggregate root with the given type.
     */
    fun <T : AggregateRoot?> createAggregateRoot(aggregateRootType: Class<T>): T
}