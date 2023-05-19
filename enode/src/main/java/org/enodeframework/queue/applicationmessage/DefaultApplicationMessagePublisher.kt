package org.enodeframework.queue.applicationmessage

import org.enodeframework.common.serializing.SerializeService
import org.enodeframework.common.utils.Assert
import org.enodeframework.infrastructure.TypeNameProvider
import org.enodeframework.messaging.ApplicationMessage
import org.enodeframework.messaging.MessagePublisher
import org.enodeframework.queue.MessageTypeCode
import org.enodeframework.queue.QueueMessage
import org.enodeframework.queue.SendMessageResult
import org.enodeframework.queue.SendMessageService
import java.util.concurrent.CompletableFuture

class DefaultApplicationMessagePublisher(
    private val topic: String,
    private val tag: String,
    private val producer: SendMessageService,
    private val serializeService: SerializeService,
    private val typeNameProvider: TypeNameProvider
) : MessagePublisher<ApplicationMessage> {
    private fun createApplicationMessage(message: ApplicationMessage): QueueMessage {
        Assert.nonNull(topic, "topic")
        val appMessageData = serializeService.serialize(message)
        val applicationMessage = GenericApplicationMessage()
        applicationMessage.applicationMessageData = appMessageData
        applicationMessage.applicationMessageType = typeNameProvider.getTypeName(message.javaClass)
        val data = serializeService.serialize(applicationMessage)
        val routeKey = message.id
        val queueMessage = QueueMessage()
        queueMessage.body = data
        queueMessage.routeKey = routeKey
        queueMessage.key = message.id
        queueMessage.topic = topic
        queueMessage.tag = tag
        queueMessage.type = MessageTypeCode.ApplicationMessage.value
        return queueMessage
    }

    override fun publishAsync(message: ApplicationMessage): CompletableFuture<SendMessageResult> {
        return producer.sendMessageAsync(createApplicationMessage(message))
    }
}
