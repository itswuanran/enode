package org.enodeframework.infrastructure

interface AssemblyInitializer {
    fun initialize(componentTypes: Set<Class<*>>)
}