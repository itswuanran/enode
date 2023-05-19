package org.enodeframework.queue.domainevent

import com.google.common.base.Strings
import org.enodeframework.common.io.Task
import org.enodeframework.common.serializing.SerializeService
import org.enodeframework.configurations.SysProperties
import org.enodeframework.eventing.*
import org.enodeframework.queue.MessageContext
import org.enodeframework.queue.MessageHandler
import org.enodeframework.queue.QueueMessage
import org.enodeframework.queue.SendReplyService
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture

class DefaultDomainEventMessageHandler(
    val sendReplyService: SendReplyService,
    private val domainEventMessageProcessor: ProcessingEventProcessor,
    private val eventSerializer: EventSerializer,
    private val serializeService: SerializeService
) : MessageHandler {
    var isSendEventHandledMessage = true

    override fun handle(queueMessage: QueueMessage, context: MessageContext) {
        logger.info("Received event stream message: {}", queueMessage)
        val message = serializeService.deserialize(queueMessage.body, GenericDomainEventMessage::class.java)
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

    internal class DomainEventStreamProcessContext(
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
            val address = Optional.ofNullable<Map<String, Any>>(
                domainEventStreamMessage.items
            ).map { x: Map<String, Any> -> x[SysProperties.ITEMS_COMMAND_REPLY_ADDRESS_KEY] as String? }
                .orElse("")
            if (Strings.isNullOrEmpty(address)) {
                return Task.completedTask
            }
            val commandResult = Optional.ofNullable<Map<String, Any>>(
                domainEventStreamMessage.items
            ).map { x: Map<String, Any> -> x[SysProperties.ITEMS_COMMAND_RESULT_KEY] as String? }
                .orElse("")
            val domainEventHandledMessage = DomainEventHandledMessage()
            domainEventHandledMessage.commandId = domainEventStreamMessage.commandId
            domainEventHandledMessage.aggregateRootId = domainEventStreamMessage.aggregateRootId
            domainEventHandledMessage.commandResult = commandResult
            return eventConsumer.sendReplyService.sendEventReply(domainEventHandledMessage, address!!)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultDomainEventMessageHandler::class.java)
    }
}
