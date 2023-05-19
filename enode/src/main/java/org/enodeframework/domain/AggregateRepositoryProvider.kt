package org.enodeframework.domain

/**
 * Represents a provider to provide the aggregate repository.
 */
interface AggregateRepositoryProvider {
    /**
     * Get the aggregateRepository for the given aggregate type.
     */
    fun getRepository(aggregateRootType: Class<out AggregateRoot?>): AggregateRepositoryProxy
}