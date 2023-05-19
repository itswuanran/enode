package org.enodeframework.infrastructure

import org.enodeframework.common.extensions.ObjectContainer

interface AssemblyInitializer {
    fun initialize(objectContainer: ObjectContainer, componentTypes: Set<Class<*>>)
}