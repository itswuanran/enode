package org.enodeframework.domain

/**
 * Defines a factory to create empty aggregate root.
 */
interface IAggregateRootFactory {
    /**
     * Create an empty aggregate root with the given type.
     */
    fun <T : IAggregateRoot?> createAggregateRoot(aggregateRootType: Class<T>): T
}