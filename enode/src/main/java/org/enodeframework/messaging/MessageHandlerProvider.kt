package org.enodeframework.messaging

interface MessageHandlerProvider {
    fun getHandlers(messageType: Class<*>): List<MessageHandlerData<MessageHandlerProxy1>>
}