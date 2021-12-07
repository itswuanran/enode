package org.enodeframework.messaging.impl

import org.enodeframework.eventing.DomainEventMessage
import org.enodeframework.infrastructure.impl.AbstractHandlerProvider
import org.enodeframework.messaging.Message
import org.enodeframework.messaging.MessageHandlerData
import org.enodeframework.messaging.MessageHandlerProvider
import org.enodeframework.messaging.MessageHandlerProxy1
import java.lang.reflect.Method
import kotlin.coroutines.Continuation

/**
 * @author anruence@gmail.com
 */
class DefaultMessageHandlerProvider : AbstractHandlerProvider<Class<*>, MessageHandlerProxy1, Class<*>>(),
    MessageHandlerProvider {
    override fun getKey(method: Method): Class<*> {
        return method.parameterTypes[0]
    }

    override fun getHandlerProxyImplementationType(): Class<out MessageHandlerProxy1> {
        return DefaultMessageHandlerProxy1::class.java
    }

    override fun isHandleMethodMatch(method: Method): Boolean {
        if (isSuspendMethod(method)) {
            return methodMatchSuspend(method)
        }
        return methodMatch(method)
    }

    private fun methodMatch(method: Method): Boolean {
        if (method.parameterTypes.size != 1) {
            return false
        }
        if (Message::class.java == method.parameterTypes[0]) {
            return false
        }
        if (!DomainEventMessage::class.java.isAssignableFrom(method.parameterTypes[0])) {
            return false
        }
        return isMethodAnnotationSubscribe(method)
    }

    private fun methodMatchSuspend(method: Method): Boolean {
        if (method.parameterTypes.size != 2) {
            return false
        }
        if (Message::class.java == method.parameterTypes[0]) {
            return false
        }
        if (!DomainEventMessage::class.java.isAssignableFrom(method.parameterTypes[0])) {
            return false
        }
        if (Continuation::class.java != method.parameterTypes[1]) {
            return false
        }
        return isMethodAnnotationSubscribe(method)
    }

    override fun isHandlerSourceMatchKey(handlerSource: Class<*>, key: Class<*>): Boolean {
        return key == handlerSource
    }

    override fun getHandlers(messageType: Class<*>): List<MessageHandlerData<MessageHandlerProxy1>> {
        return getHandlersInternal(messageType)
    }

    override fun isHandleRegisterOnce(): Boolean {
        return false
    }
}