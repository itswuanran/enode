package org.enodeframework.domain.impl

import org.enodeframework.common.exception.HandlerNotFoundException
import org.enodeframework.common.function.Action2
import org.enodeframework.configurations.SysProperties
import org.enodeframework.domain.AggregateRoot
import org.enodeframework.domain.AggregateRootInternalHandlerProvider
import org.enodeframework.eventing.DomainEventMessage
import org.enodeframework.infrastructure.AssemblyInitializer
import org.enodeframework.infrastructure.TypeUtils
import java.lang.invoke.MethodHandles
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.*

/**
 * @author anruence@gmail.com
 */
class DefaultAggregateRootInternalHandlerProvider : AggregateRootInternalHandlerProvider, AssemblyInitializer {

    private val aggregateRootInternalHandlerMap: MutableMap<Class<*>, MutableMap<Class<*>, Action2<AggregateRoot, DomainEventMessage<*>>>> =
        HashMap()

    override fun initialize(componentTypes: Set<Class<*>>) {
        componentTypes.filter { type: Class<*> -> TypeUtils.isAggregateRoot(type) }
            .forEach { aggregateRootType: Class<*> -> recurseRegisterInternalHandler(aggregateRootType) }
    }

    private fun recurseRegisterInternalHandler(aggregateRootType: Class<*>) {
        val superclass = aggregateRootType.superclass
        if (!isInterfaceOrObjectClass(superclass)) {
            registerInternalHandlerWithSuperclass(aggregateRootType, superclass)
        }
        register(aggregateRootType, aggregateRootType)
    }

    private fun registerInternalHandlerWithSuperclass(aggregateRootType: Class<*>, parentType: Class<*>) {
        val superclass = parentType.superclass
        if (!isInterfaceOrObjectClass(superclass)) {
            registerInternalHandlerWithSuperclass(aggregateRootType, superclass)
        }
        register(aggregateRootType, parentType)
    }

    private fun isInterfaceOrObjectClass(type: Class<*>): Boolean {
        return Modifier.isInterface(type.modifiers) || type == Any::class.java
    }

    private fun register(aggregateRootType: Class<*>, type: Class<*>) {
        Arrays.stream(type.declaredMethods).filter { method: Method ->
            method.name.startsWith(SysProperties.AGGREGATE_ROOT_HANDLE_METHOD_NAME)
                    && method.parameterTypes.size == 1 && DomainEventMessage::class.java.isAssignableFrom(method.parameterTypes[0])
        }.forEach { method ->
            registerInternalHandler(aggregateRootType, method.parameterTypes[0], method)
        }
    }

    private fun registerInternalHandler(aggregateRootType: Class<*>, eventType: Class<*>, method: Method) {
        val eventHandlerDic = aggregateRootInternalHandlerMap.computeIfAbsent(aggregateRootType) { HashMap() }
        method.isAccessible = true
        val methodHandle = MethodHandles.lookup().unreflect(method)
        eventHandlerDic[eventType] =
            Action2 { aggregateRoot: AggregateRoot, domainEventMessage: DomainEventMessage<*> ->
                methodHandle.invoke(aggregateRoot, domainEventMessage)
            }
    }

    override fun getInternalEventHandler(
        aggregateRootType: Class<out AggregateRoot>,
        eventType: Class<out DomainEventMessage<*>>
    ): Action2<AggregateRoot, DomainEventMessage<*>> {
        var currentAggregateType = aggregateRootType
        while (true) {
            val handler = aggregateRootInternalHandlerMap[currentAggregateType]?.get(eventType)
            if (handler != null) {
                return handler
            }
            if (currentAggregateType.superclass != null
                && listOf(*currentAggregateType.superclass.interfaces).contains(AggregateRoot::class.java)
            ) {
                currentAggregateType = currentAggregateType.superclass as Class<out AggregateRoot>
            } else {
                break
            }
        }
        throw HandlerNotFoundException(
            String.format(
                "Could not find event handler for [%s] of [%s]",
                eventType.javaClass.name,
                aggregateRootType.javaClass.name
            )
        )
    }
}