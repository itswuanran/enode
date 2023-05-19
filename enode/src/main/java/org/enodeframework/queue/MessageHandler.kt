package org.enodeframework.queue

interface MessageHandler {
    /**
     * message queue handler
     */
    fun handle(queueMessage: QueueMessage, context: MessageContext)
}
