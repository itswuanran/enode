package org.enodeframework.queue.domainevent;

import org.enodeframework.common.serializing.SerializeService;
import org.enodeframework.common.utils.Assert;
import org.enodeframework.eventing.DomainEventStream;
import org.enodeframework.eventing.EventSerializer;
import org.enodeframework.messaging.MessagePublisher;
import org.enodeframework.queue.QueueMessage;
import org.enodeframework.queue.SendMessageService;

import java.util.concurrent.CompletableFuture;

public class DefaultDomainEventPublisher implements MessagePublisher<DomainEventStream> {
    private final String topic;
    private final String tag;
    private final EventSerializer eventSerializer;
    private final SendMessageService sendMessageService;
    private final SerializeService serializeService;

    public DefaultDomainEventPublisher(String topic, String tag, EventSerializer eventSerializer, SendMessageService sendMessageService, SerializeService serializeService) {
        this.eventSerializer = eventSerializer;
        this.sendMessageService = sendMessageService;
        this.topic = topic;
        this.tag = tag;
        this.serializeService = serializeService;
    }

    protected QueueMessage createDomainEventStreamMessage(DomainEventStream eventStream) {
        Assert.nonNull(eventStream.getAggregateRootId(), "aggregateRootId");
        Assert.nonNull(topic, "topic");
        GenericDomainEventMessage message = new GenericDomainEventMessage();
        message.setId(eventStream.getId());
        message.setCommandId(eventStream.getCommandId());
        message.setAggregateRootTypeName(eventStream.getAggregateRootTypeName());
        message.setAggregateRootId(eventStream.getAggregateRootId());
        message.setTimestamp(eventStream.getTimestamp());
        message.setVersion(eventStream.getVersion());
        message.setEvents(eventSerializer.serialize(eventStream.getEvents()));
        message.setItems(eventStream.getItems());
        String data = serializeService.serialize(message);
        String routeKey = message.getAggregateRootId();
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setTopic(topic);
        queueMessage.setTag(tag);
        queueMessage.setBody(data);
        queueMessage.setRouteKey(routeKey);
        queueMessage.setKey(String.format("%s_evt_agg_%s", message.getId(), message.getAggregateRootId()));
        return queueMessage;
    }

    @Override
    public CompletableFuture<Boolean> publishAsync(DomainEventStream message) {
        return sendMessageService.sendMessageAsync(createDomainEventStreamMessage(message));
    }
}
