package org.enodeframework.messaging

interface TwoMessageHandlerProvider {
    fun getHandlers(messageTypes: List<Class<*>>): List<MessageHandlerData<MessageHandlerProxy2>>
}