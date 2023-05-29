package org.enodeframework.messaging.impl

import org.enodeframework.infrastructure.ObjectProxy
import org.enodeframework.infrastructure.TypeNameProvider
import org.enodeframework.messaging.Message
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class MultiMessageDispatching(
    messages: List<Message>,
    handlers: List<ObjectProxy>,
    rootDispatching: RootDispatching,
    typeNameProvider: TypeNameProvider
) {
    val messages: Array<Message>
    private val handlerDict: ConcurrentMap<String, ObjectProxy>
    private val rootDispatching: RootDispatching

    init {
        this.messages = messages.toTypedArray<Message>()
        handlerDict = ConcurrentHashMap()
        handlers.forEach { objectProxy ->
            handlerDict.putIfAbsent(
                typeNameProvider.getTypeName(objectProxy.getInnerObject().javaClass),
                objectProxy
            )
        }
        this.rootDispatching = rootDispatching
        this.rootDispatching.addChildDispatching(this)
    }

    fun removeHandledHandler(handlerTypeName: String) {
        if (handlerDict.remove(handlerTypeName) != null) {
            if (handlerDict.isEmpty()) {
                rootDispatching.onChildDispatchingFinished(this)
            }
        }
    }
}
