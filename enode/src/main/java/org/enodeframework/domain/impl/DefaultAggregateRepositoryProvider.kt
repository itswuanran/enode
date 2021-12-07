package org.enodeframework.domain.impl

import org.enodeframework.common.container.DefaultObjectContainer
import org.enodeframework.domain.AggregateRepository
import org.enodeframework.domain.AggregateRepositoryProvider
import org.enodeframework.domain.AggregateRepositoryProxy
import org.enodeframework.domain.AggregateRoot
import org.enodeframework.infrastructure.AssemblyInitializer
import org.enodeframework.infrastructure.TypeUtils
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * @author anruence@gmail.com
 */
class DefaultAggregateRepositoryProvider : AggregateRepositoryProvider, AssemblyInitializer {
    private val repositoryDict: MutableMap<Class<*>, AggregateRepositoryProxy> = HashMap()
    override fun getRepository(aggregateRootType: Class<out AggregateRoot>): AggregateRepositoryProxy? {
        return repositoryDict[aggregateRootType]
    }

    override fun initialize(componentTypes: Set<Class<*>>) {
        componentTypes.filter { type: Class<*> -> TypeUtils.isAggregateRepositoryType(type) }
            .forEach { aggregateRepositoryType: Class<*> -> registerAggregateRepository(aggregateRepositoryType) }
    }

    /**
     * 获取继承AggregateRoot的class，IAggregateRepository接口的泛型
     */
    private fun registerAggregateRepository(aggregateRepositoryType: Class<*>) {
        aggregateRepositoryType.genericInterfaces.forEach { type: Type ->
            val superGenericInterfaceType = type as ParameterizedType
            if (AggregateRepository::class.java != superGenericInterfaceType.rawType) {
                return
            }
            val aggregateRepositoryProxy = DefaultAggregateRepositoryProxy<AggregateRoot>()
            aggregateRepositoryProxy.setInnerObject(DefaultObjectContainer.resolve(aggregateRepositoryType))
            repositoryDict[superGenericInterfaceType.actualTypeArguments[0] as Class<*>] = aggregateRepositoryProxy
        }
    }
}