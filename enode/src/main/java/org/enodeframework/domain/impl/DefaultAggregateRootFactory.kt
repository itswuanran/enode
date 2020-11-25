package org.enodeframework.domain.impl

import org.enodeframework.common.exception.AggregateRootCreateException
import org.enodeframework.domain.IAggregateRoot
import org.enodeframework.domain.IAggregateRootFactory

/**
 * @author anruence@gmail.com
 */
class DefaultAggregateRootFactory : IAggregateRootFactory {
    override fun <T : IAggregateRoot?> createAggregateRoot(aggregateRootType: Class<T>): T {
        return try {
            aggregateRootType.getDeclaredConstructor().newInstance()
        } catch (e: Exception) {
            throw AggregateRootCreateException(e)
        }
    }
}