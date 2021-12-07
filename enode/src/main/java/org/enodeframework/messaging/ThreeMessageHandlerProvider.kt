package org.enodeframework.messaging

interface ThreeMessageHandlerProvider {
    fun getHandlers(messageTypes: List<Class<*>>): List<MessageHandlerData<MessageHandlerProxy3>>
}