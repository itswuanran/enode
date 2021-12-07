package org.enodeframework.infrastructure

import org.enodeframework.domain.AggregateRepository
import org.enodeframework.domain.AggregateRoot
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType

/**
 * @author anruence@gmail.com
 */
object TypeUtils {
    fun isAggregateRoot(type: Class<*>): Boolean {
        return !Modifier.isAbstract(type.modifiers) && AggregateRoot::class.java.isAssignableFrom(type)
    }

    fun isAggregateRepositoryType(type: Class<*>): Boolean {
        return !Modifier.isAbstract(type.modifiers) && AggregateRepository::class.java.isAssignableFrom(type)
    }

    fun getGenericType(clazz: Class<*>): Class<*> {
        return (clazz.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<*>
    }
}