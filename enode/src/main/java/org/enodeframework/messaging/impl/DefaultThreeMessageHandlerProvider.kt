package org.enodeframework.messaging.impl

import org.enodeframework.eventing.DomainEventMessage
import org.enodeframework.infrastructure.impl.AbstractHandlerProvider
import org.enodeframework.infrastructure.impl.ManyType
import org.enodeframework.messaging.Message
import org.enodeframework.messaging.MessageHandlerData
import org.enodeframework.messaging.MessageHandlerProxy3
import org.enodeframework.messaging.ThreeMessageHandlerProvider
import java.lang.reflect.Method
import kotlin.coroutines.Continuation

/**
 * @author anruence@gmail.com
 */
class DefaultThreeMessageHandlerProvider : AbstractHandlerProvider<ManyType, MessageHandlerProxy3, List<Class<*>>>(),
    ThreeMessageHandlerProvider {

    override fun getKey(method: Method): ManyType {
        return ManyType(listOf(*method.parameterTypes))
    }

    override fun isHandleRegisterOnce(): Boolean {
        return false
    }

    override fun getHandlerProxyImplementationType(): Class<out MessageHandlerProxy3> {
        return DefaultMessageHandlerProxy3::class.java
    }

    override fun isHandlerSourceMatchKey(handlerSource: List<Class<*>>, key: ManyType): Boolean {
        for (type in key.types) {
            if (!handlerSource.any { x: Class<*> -> x == type }) {
                return false
            }
        }
        return true
    }

    override fun isHandleMethodMatch(method: Method): Boolean {
        if (isSuspendMethod(method)) {
            return methodMatchSuspend(method)
        }
        return methodMatch(method)
    }

    private fun methodMatch(method: Method): Boolean {
        if (method.parameterTypes.size != 3) {
            return false
        }
        if (Message::class.java == method.parameterTypes[0]) {
            return false
        }
        if (Message::class.java == method.parameterTypes[1]) {
            return false
        }
        if (Message::class.java == method.parameterTypes[2]) {
            return false
        }
        if (!DomainEventMessage::class.java.isAssignableFrom(method.parameterTypes[0])) {
            return false
        }
        if (!DomainEventMessage::class.java.isAssignableFrom(method.parameterTypes[1])) {
            return false
        }
        if (!DomainEventMessage::class.java.isAssignableFrom(method.parameterTypes[2])) {
            return false
        }
        return isMethodAnnotationSubscribe(method)
    }

    private fun methodMatchSuspend(method: Method): Boolean {
        if (method.parameterTypes.size != 4) {
            return false
        }
        if (Message::class.java == method.parameterTypes[0]) {
            return false
        }
        if (Message::class.java == method.parameterTypes[1]) {
            return false
        }
        if (Message::class.java == method.parameterTypes[2]) {
            return false
        }
        if (!DomainEventMessage::class.java.isAssignableFrom(method.parameterTypes[0])) {
            return false
        }
        if (!DomainEventMessage::class.java.isAssignableFrom(method.parameterTypes[1])) {
            return false
        }
        if (!DomainEventMessage::class.java.isAssignableFrom(method.parameterTypes[2])) {
            return false
        }
        if (Continuation::class.java != method.parameterTypes[3]) {
            return false
        }
        return isMethodAnnotationSubscribe(method)
    }

    override fun getHandlers(messageTypes: List<Class<*>>): List<MessageHandlerData<MessageHandlerProxy3>> {
        return getHandlersInternal(messageTypes)
    }
}