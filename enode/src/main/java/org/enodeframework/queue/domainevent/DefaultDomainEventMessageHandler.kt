package org.enodeframework.queue.domainevent

import com.google.common.base.Strings
import org.enodeframework.commanding.CommandReturnType
import org.enodeframework.commanding.CommandStatus
import org.enodeframework.common.extensions.SysProperties
import org.enodeframework.common.io.Task
import org.enodeframework.common.serializing.SerializeService
import org.enodeframework.eventing.DomainEventStream
import org.enodeframework.eventing.EventProcessContext
import org.enodeframework.eventing.EventSerializer
import org.enodeframework.eventing.ProcessingEvent
import org.enodeframework.eventing.ProcessingEventProcessor
import org.enodeframework.queue.MessageContext
import org.enodeframework.queue.MessageHandler
import org.enodeframework.queue.QueueMessage
import org.enodeframework.queue.SendReplyService
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

class DefaultDomainEventMessageHandler(
    private val sendReplyService: SendReplyService,
    private val domainEventMessageProcessor: ProcessingEventProcessor,
    private val eventSerializer: EventSerializer,
    private val serializeService: SerializeService,
    private var isSendEventHandledMessage: Boolean = true
) : MessageHandler {
    constructor(
        sendReplyService: SendReplyService,
        domainEventMessageProcessor: ProcessingEventProcessor,
        eventSerializer: EventSerializer,
        serializeService: SerializeService
    ) : this(sendReplyService, domainEventMessageProcessor, eventSerializer, serializeService, true)

    private val logger = LoggerFactory.getLogger(DefaultDomainEventMessageHandler::class.java)

    override fun handle(queueMessage: QueueMessage, context: MessageContext) {
        logger.info("Received event stream message: {}", queueMessage)
        val message = serializeService.deserializeBytes(queueMessage.body, GenericDomainEventMessage::class.java)
        val domainEventStreamMessage = convertToDomainEventStream(message)
        val processContext = DomainEventStreamProcessContext(this, domainEventStreamMessage, queueMessage, context)
        val processingMessage = ProcessingEvent(domainEventStreamMessage, processContext)
        domainEventMessageProcessor.process(processingMessage)
    }

    private fun convertToDomainEventStream(message: GenericDomainEventMessage): DomainEventStream {
        val domainEventStreamMessage = DomainEventStream(
            message.commandId,
            message.aggregateRootId,
            message.version,
            message.aggregateRootTypeName,
            eventSerializer.deserialize(message.events),
            message.items
        )
        domainEventStreamMessage.id = message.id
        domainEventStreamMessage.timestamp = message.timestamp
        return domainEventStreamMessage
    }

    class DomainEventStreamProcessContext(
        private val eventConsumer: DefaultDomainEventMessageHandler,
        private val domainEventStreamMessage: DomainEventStream,
        private val queueMessage: QueueMessage,
        private val messageContext: MessageContext
    ) : EventProcessContext {
        override fun notifyEventProcessed(): CompletableFuture<Boolean> {
            messageContext.onMessageHandled(queueMessage)
            if (!eventConsumer.isSendEventHandledMessage) {
                return Task.completedTask
            }
            val address = domainEventStreamMessage.items[SysProperties.ITEMS_COMMAND_REPLY_ADDRESS_KEY] as String?
            if (Strings.isNullOrEmpty(address)) {
                return Task.completedTask
            }
            val commandResult = domainEventStreamMessage.items[SysProperties.ITEMS_COMMAND_RESULT_KEY] as String?
            val replyMessage = DomainEventHandledMessage()
            replyMessage.commandId = domainEventStreamMessage.commandId
            replyMessage.aggregateRootId = domainEventStreamMessage.aggregateRootId
            replyMessage.result = commandResult ?: ""
            replyMessage.address = address ?: ""
            replyMessage.status = CommandStatus.Success
            replyMessage.returnType = CommandReturnType.EventHandled
            return eventConsumer.sendReplyService.send(replyMessage).thenApply { true }
        }
    }

}
