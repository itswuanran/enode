package org.enodeframework.domain.impl

import org.enodeframework.domain.AggregateRepository
import org.enodeframework.domain.AggregateRepositoryProxy
import org.enodeframework.domain.AggregateRoot
import java.util.concurrent.CompletableFuture

/**
 * @author anruence@gmail.com
 */
class DefaultAggregateRepositoryProxy<T : AggregateRoot?> :
    AggregateRepositoryProxy {
    private lateinit var aggregateRepository: AggregateRepository<T>

    override fun getInnerObject(): Any {
        return aggregateRepository
    }

    override fun setInnerObject(innerObject: Any) {
        aggregateRepository = innerObject as AggregateRepository<T>
    }

    override fun <T : AggregateRoot?> getAsync(aggregateRootId: String): CompletableFuture<T> {
        return aggregateRepository.getAsync(aggregateRootId)
    }
}