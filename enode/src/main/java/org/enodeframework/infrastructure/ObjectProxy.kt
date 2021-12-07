package org.enodeframework.infrastructure

interface ObjectProxy {
    fun getInnerObject(): Any
    fun setInnerObject(innerObject: Any)
}