package org.enodeframework.messaging

import org.enodeframework.infrastructure.MethodInvocation
import org.enodeframework.infrastructure.ObjectProxy

interface MessageHandlerProxy3 : ObjectProxy, MethodInvocation {
    suspend fun handleAsync(message1: Message, message2: Message, message3: Message)
}