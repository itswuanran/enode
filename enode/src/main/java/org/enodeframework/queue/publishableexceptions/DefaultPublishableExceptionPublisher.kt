package org.enodeframework.queue.publishableexceptions

import org.enodeframework.common.serializing.SerializeService
import org.enodeframework.common.utils.Assert
import org.enodeframework.domain.DomainExceptionMessage
import org.enodeframework.infrastructure.TypeNameProvider
import org.enodeframework.messaging.MessagePublisher
import org.enodeframework.queue.MessageTypeCode
import org.enodeframework.queue.QueueMessage
import org.enodeframework.queue.SendMessageService
import java.util.concurrent.CompletableFuture

class DefaultPublishableExceptionPublisher(
    private val topic: String,
    private val tag: String,
    private val sendMessageService: SendMessageService,
    private val serializeService: SerializeService,
    private val typeNameProvider: TypeNameProvider
) : MessagePublisher<DomainExceptionMessage> {
    protected fun createExceptionMessage(exception: DomainExceptionMessage): QueueMessage {
        Assert.nonNull(topic, "topic")
        val serializableInfo: MutableMap<String, Any> = HashMap()
        exception.serializeTo(serializableInfo)
        val exceptionMessage = GenericPublishableExceptionMessage()
        exceptionMessage.uniqueId = exception.id
        exceptionMessage.exceptionType = typeNameProvider.getTypeName(exception.javaClass)
        exceptionMessage.timestamp = exception.timestamp
        exceptionMessage.serializableInfo = serializableInfo
        exceptionMessage.items = exception.items
        val data = serializeService.serialize(exceptionMessage)
        val routeKey = exception.id
        val queueMessage = QueueMessage()
        queueMessage.topic = topic
        queueMessage.tag = tag
        queueMessage.body = data
        queueMessage.routeKey = routeKey
        queueMessage.type = MessageTypeCode.ExceptionMessage.value
        queueMessage.key = exceptionMessage.uniqueId
        return queueMessage
    }

    override fun publishAsync(message: DomainExceptionMessage): CompletableFuture<Boolean> {
        return sendMessageService.sendMessageAsync(createExceptionMessage(message))
    }
}
