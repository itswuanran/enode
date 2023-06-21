package org.enodeframework.queue

import com.google.common.collect.Maps
import org.slf4j.LoggerFactory

class MessageHandlerHolder(private val messageHandlerMap: MutableMap<String, MessageHandler> = Maps.newHashMap()) {

    fun put(type: String, handler: MessageHandler): MessageHandlerHolder {
        messageHandlerMap[type] = handler
        return this
    }

    fun chooseMessageHandler(type: String): MessageHandler {
        val handler = messageHandlerMap[type]
        return handler ?: NoOpMessageHandler()
    }
}

class NoOpMessageHandler : MessageHandler {
    private val logger = LoggerFactory.getLogger(NoOpMessageHandler::class.java)
    override fun handle(queueMessage: QueueMessage, context: MessageContext) {
        logger.error("Received no route message: {}", queueMessage)
        context.onMessageHandled(queueMessage)
    }
}
