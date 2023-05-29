package org.enodeframework.queue.publishableexceptions

import org.enodeframework.common.exception.MessageInstanceCreateException
import org.enodeframework.common.serializing.SerializeService
import org.enodeframework.domain.DomainExceptionMessage
import org.enodeframework.infrastructure.TypeNameProvider
import org.enodeframework.messaging.MessageDispatcher
import org.enodeframework.queue.MessageContext
import org.enodeframework.queue.MessageHandler
import org.enodeframework.queue.QueueMessage
import org.slf4j.LoggerFactory

class DefaultPublishableExceptionMessageHandler(
    private val typeNameProvider: TypeNameProvider,
    private val messageDispatcher: MessageDispatcher,
    private val serializeService: SerializeService
) : MessageHandler {
    private val logger = LoggerFactory.getLogger(DefaultPublishableExceptionMessageHandler::class.java)

    override fun handle(queueMessage: QueueMessage, context: MessageContext) {
        logger.info("Received domain exception message: {}", queueMessage)
        val exceptionMessage =
            serializeService.deserialize(queueMessage.body, GenericPublishableExceptionMessage::class.java)
        val exceptionType = typeNameProvider.getType(exceptionMessage.exceptionType)
        val exception: DomainExceptionMessage = try {
            exceptionType.getDeclaredConstructor().newInstance() as DomainExceptionMessage
        } catch (e: Exception) {
            throw MessageInstanceCreateException(e)
        }
        exception.id = exceptionMessage.uniqueId
        exception.timestamp = exceptionMessage.timestamp
        exception.items = exceptionMessage.items
        exception.restoreFrom(exceptionMessage.serializableInfo)
        messageDispatcher.dispatchMessageAsync(exception)
            .whenComplete { _, _ -> context.onMessageHandled(queueMessage) }
    }
}
