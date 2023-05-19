package org.enodeframework.infrastructure

import org.enodeframework.common.container.ObjectContainer

interface AssemblyInitializer {
    fun initialize(objectContainer: ObjectContainer, componentTypes: Set<Class<*>>)
}