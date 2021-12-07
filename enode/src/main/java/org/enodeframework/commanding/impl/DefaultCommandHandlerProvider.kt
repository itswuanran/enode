package org.enodeframework.commanding.impl

import org.enodeframework.commanding.CommandContext
import org.enodeframework.commanding.CommandHandlerProvider
import org.enodeframework.commanding.CommandHandlerProxy
import org.enodeframework.commanding.CommandMessage
import org.enodeframework.infrastructure.impl.AbstractHandlerProvider
import org.enodeframework.messaging.MessageHandlerData
import java.lang.reflect.Method
import kotlin.coroutines.Continuation

/**
 * @author anruence@gmail.com
 */
class DefaultCommandHandlerProvider : AbstractHandlerProvider<Class<*>, CommandHandlerProxy, Class<*>>(),
    CommandHandlerProvider {
    override fun getKey(method: Method): Class<*> {
        return method.parameterTypes[1]
    }

    override fun getHandlerProxyImplementationType(): Class<out CommandHandlerProxy> {
        return DefaultCommandHandlerProxy::class.java
    }

    override fun isHandlerSourceMatchKey(handlerSource: Class<*>, key: Class<*>): Boolean {
        return key == handlerSource
    }

    override fun isHandleMethodMatch(method: Method): Boolean {
        if (isSuspendMethod(method)) {
            return methodMatchSuspend(method)
        }
        return methodMatch(method)
    }

    private fun methodMatch(method: Method): Boolean {
        if (method.parameterTypes.size != 2) {
            return false
        }
        if (CommandContext::class.java != method.parameterTypes[0]) {
            return false
        }
        if (CommandMessage::class.java == method.parameterTypes[1]) {
            return false
        }
        if (!CommandMessage::class.java.isAssignableFrom(method.parameterTypes[1])) {
            return false
        }
        return isMethodAnnotationSubscribe(method)
    }

    private fun methodMatchSuspend(method: Method): Boolean {
        if (method.parameterTypes.size != 3) {
            return false
        }
        if (CommandContext::class.java != method.parameterTypes[0]) {
            return false
        }
        if (CommandMessage::class.java == method.parameterTypes[1]) {
            return false
        }
        if (!CommandMessage::class.java.isAssignableFrom(method.parameterTypes[1])) {
            return false
        }
        if (Continuation::class.java != method.parameterTypes[2]) {
            return false
        }
        return isMethodAnnotationSubscribe(method)
    }

    override fun getHandlers(commandType: Class<*>): List<MessageHandlerData<CommandHandlerProxy>> {
        return getHandlersInternal(commandType)
    }

    override fun isHandleRegisterOnce(): Boolean {
        return true
    }
}