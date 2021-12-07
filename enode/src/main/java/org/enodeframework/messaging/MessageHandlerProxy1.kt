package org.enodeframework.messaging

import org.enodeframework.infrastructure.MethodInvocation
import org.enodeframework.infrastructure.ObjectProxy

interface MessageHandlerProxy1 : ObjectProxy, MethodInvocation {
    suspend fun handleAsync(message: Message)
}