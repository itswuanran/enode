package org.enodeframework.queue.domainevent;

import com.google.common.base.Strings;
import org.enodeframework.common.io.Task;
import org.enodeframework.common.serializing.SerializeService;
import org.enodeframework.configurations.SysProperties;
import org.enodeframework.eventing.DomainEventStream;
import org.enodeframework.eventing.EventProcessContext;
import org.enodeframework.eventing.EventSerializer;
import org.enodeframework.eventing.ProcessingEvent;
import org.enodeframework.eventing.ProcessingEventProcessor;
import org.enodeframework.queue.MessageContext;
import org.enodeframework.queue.MessageHandler;
import org.enodeframework.queue.QueueMessage;
import org.enodeframework.queue.SendReplyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DefaultDomainEventMessageHandler implements MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(DefaultDomainEventMessageHandler.class);

    private final SendReplyService sendReplyService;

    private final EventSerializer eventSerializer;

    private final ProcessingEventProcessor domainEventMessageProcessor;

    private final SerializeService serializeService;

    private boolean sendEventHandledMessage = true;

    public DefaultDomainEventMessageHandler(SendReplyService sendReplyService, ProcessingEventProcessor domainEventMessageProcessor, EventSerializer eventSerializer, SerializeService serializeService) {
        this.sendReplyService = sendReplyService;
        this.eventSerializer = eventSerializer;
        this.domainEventMessageProcessor = domainEventMessageProcessor;
        this.serializeService = serializeService;
    }

    public SendReplyService getSendReplyService() {
        return sendReplyService;
    }

    @Override
    public void handle(QueueMessage queueMessage, MessageContext context) {
        logger.info("Received event stream message: {}", queueMessage);
        GenericDomainEventMessage message = serializeService.deserialize(queueMessage.getBody(), GenericDomainEventMessage.class);
        DomainEventStream domainEventStreamMessage = convertToDomainEventStream(message);
        DomainEventStreamProcessContext processContext = new DomainEventStreamProcessContext(this, domainEventStreamMessage, queueMessage, context);
        ProcessingEvent processingMessage = new ProcessingEvent(domainEventStreamMessage, processContext);
        domainEventMessageProcessor.process(processingMessage);
    }

    private DomainEventStream convertToDomainEventStream(GenericDomainEventMessage message) {
        DomainEventStream domainEventStreamMessage = new DomainEventStream(
            message.getCommandId(),
            message.getAggregateRootId(),
            message.getVersion(),
            message.getAggregateRootTypeName(),
            eventSerializer.deserialize(message.getEvents()),
            message.getItems()
        );
        domainEventStreamMessage.setId(message.getId());
        domainEventStreamMessage.setTimestamp(message.getTimestamp());
        return domainEventStreamMessage;
    }

    public boolean isSendEventHandledMessage() {
        return sendEventHandledMessage;
    }

    public void setSendEventHandledMessage(boolean sendEventHandledMessage) {
        this.sendEventHandledMessage = sendEventHandledMessage;
    }

    static class DomainEventStreamProcessContext implements EventProcessContext {
        private final DefaultDomainEventMessageHandler eventConsumer;
        private final DomainEventStream domainEventStreamMessage;
        private final QueueMessage queueMessage;
        private final MessageContext messageContext;

        public DomainEventStreamProcessContext(
            DefaultDomainEventMessageHandler eventConsumer,
            DomainEventStream domainEventStreamMessage,
            QueueMessage queueMessage,
            MessageContext messageContext) {
            this.eventConsumer = eventConsumer;
            this.domainEventStreamMessage = domainEventStreamMessage;
            this.queueMessage = queueMessage;
            this.messageContext = messageContext;
        }

        @Override
        public CompletableFuture<Boolean> notifyEventProcessed() {
            messageContext.onMessageHandled(queueMessage);
            if (!eventConsumer.isSendEventHandledMessage()) {
                return Task.completedTask;
            }
            String address = Optional.ofNullable(domainEventStreamMessage.getItems()).map(x -> (String) x.get(SysProperties.ITEMS_COMMAND_REPLY_ADDRESS_KEY)).orElse("");
            if (Strings.isNullOrEmpty(address)) {
                return Task.completedTask;
            }
            String commandResult = Optional.ofNullable(domainEventStreamMessage.getItems()).map(x -> (String) x.get(SysProperties.ITEMS_COMMAND_RESULT_KEY)).orElse("");
            DomainEventHandledMessage domainEventHandledMessage = new DomainEventHandledMessage();
            domainEventHandledMessage.setCommandId(domainEventStreamMessage.getCommandId());
            domainEventHandledMessage.setAggregateRootId(domainEventStreamMessage.getAggregateRootId());
            domainEventHandledMessage.setCommandResult(commandResult);
            return eventConsumer.getSendReplyService().sendEventReply(domainEventHandledMessage, address);
        }
    }
}
