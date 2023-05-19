package org.enodeframework.queue.domainevent

import org.enodeframework.common.serializing.SerializeService
import org.enodeframework.common.utils.Assert
import org.enodeframework.eventing.DomainEventStream
import org.enodeframework.eventing.EventSerializer
import org.enodeframework.messaging.MessagePublisher
import org.enodeframework.queue.MessageTypeCode
import org.enodeframework.queue.QueueMessage
import org.enodeframework.queue.SendMessageResult
import org.enodeframework.queue.SendMessageService
import java.util.concurrent.CompletableFuture

class DefaultDomainEventPublisher(
    private val topic: String,
    private val tag: String,
    private val eventSerializer: EventSerializer,
    private val sendMessageService: SendMessageService,
    private val serializeService: SerializeService
) : MessagePublisher<DomainEventStream> {
    protected fun createDomainEventStreamMessage(eventStream: DomainEventStream): QueueMessage {
        Assert.nonNull(eventStream.aggregateRootId, "aggregateRootId")
        Assert.nonNull(topic, "topic")
        val message = GenericDomainEventMessage()
        message.id = eventStream.id
        message.commandId = eventStream.commandId
        message.aggregateRootTypeName = eventStream.aggregateRootTypeName
        message.aggregateRootId = eventStream.aggregateRootId
        message.timestamp = eventStream.timestamp
        message.version = eventStream.version
        message.events = eventSerializer.serialize(eventStream.events)
        message.items = eventStream.items
        val data = serializeService.serialize(message)
        val routeKey = message.aggregateRootId
        val queueMessage = QueueMessage()
        queueMessage.topic = topic
        queueMessage.tag = tag
        queueMessage.body = data
        queueMessage.type = MessageTypeCode.DomainEventMessage.value
        queueMessage.routeKey = routeKey
        queueMessage.key = String.format("%s_evt_agg_%s", message.id, message.aggregateRootId)
        return queueMessage
    }

    override fun publishAsync(message: DomainEventStream): CompletableFuture<SendMessageResult> {
        return sendMessageService.sendMessageAsync(createDomainEventStreamMessage(message))
    }
}
