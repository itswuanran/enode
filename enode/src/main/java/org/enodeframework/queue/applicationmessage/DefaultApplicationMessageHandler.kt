package org.enodeframework.queue.applicationmessage

import org.enodeframework.common.serializing.SerializeService
import org.enodeframework.infrastructure.TypeNameProvider
import org.enodeframework.messaging.ApplicationMessage
import org.enodeframework.messaging.MessageDispatcher
import org.enodeframework.queue.MessageContext
import org.enodeframework.queue.MessageHandler
import org.enodeframework.queue.QueueMessage
import org.slf4j.LoggerFactory

class DefaultApplicationMessageHandler(
    private val typeNameProvider: TypeNameProvider,
    private val messageDispatcher: MessageDispatcher,
    private val serializeService: SerializeService
) : MessageHandler {
    override fun handle(queueMessage: QueueMessage, context: MessageContext) {
        logger.info("Received application message: {}", queueMessage)
        val appDataMessage = serializeService.deserialize(queueMessage.body, GenericApplicationMessage::class.java)
        val applicationMessageType = typeNameProvider.getType(appDataMessage.applicationMessageType)
        val message = serializeService.deserialize(
            appDataMessage.applicationMessageData,
            applicationMessageType
        ) as ApplicationMessage
        messageDispatcher.dispatchMessageAsync(message).whenComplete { _, _ -> context.onMessageHandled(queueMessage) }
    }

    private val logger = LoggerFactory.getLogger(DefaultApplicationMessageHandler::class.java)
}
