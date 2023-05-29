package org.enodeframework.messaging.impl

import org.enodeframework.infrastructure.ObjectProxy
import org.enodeframework.infrastructure.TypeNameProvider
import org.enodeframework.messaging.Message
import java.util.concurrent.ConcurrentHashMap

class SingleMessageDispatching(
    val message: Message,
    private val queueMessageDispatching: QueueMessageDispatching,
    handlers: List<ObjectProxy>,
    typeNameProvider: TypeNameProvider
) {
    private val handlerDict: ConcurrentHashMap<String, ObjectProxy> = ConcurrentHashMap()

    init {
        handlers.forEach { objectProxy ->
            handlerDict.putIfAbsent(
                typeNameProvider.getTypeName(objectProxy.getInnerObject().javaClass),
                objectProxy
            )
        }
    }

    fun removeHandledHandler(handlerTypeName: String) {
        if (handlerDict.remove(handlerTypeName) != null) {
            if (handlerDict.isEmpty()) {
                queueMessageDispatching.onMessageHandled(message)
            }
        }
    }
}
