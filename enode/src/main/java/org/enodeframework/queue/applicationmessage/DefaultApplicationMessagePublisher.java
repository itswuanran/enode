package org.enodeframework.queue.applicationmessage;

import org.enodeframework.common.serializing.SerializeService;
import org.enodeframework.common.utils.Assert;
import org.enodeframework.infrastructure.TypeNameProvider;
import org.enodeframework.messaging.ApplicationMessage;
import org.enodeframework.messaging.MessagePublisher;
import org.enodeframework.queue.MessageTypeCode;
import org.enodeframework.queue.QueueMessage;
import org.enodeframework.queue.SendMessageService;

import java.util.concurrent.CompletableFuture;

public class DefaultApplicationMessagePublisher implements MessagePublisher<ApplicationMessage> {

    private final String topic;

    private final String tag;

    private final SendMessageService producer;

    private final SerializeService serializeService;

    private final TypeNameProvider typeNameProvider;

    public DefaultApplicationMessagePublisher(String topic, String tag, SendMessageService producer, SerializeService serializeService, TypeNameProvider typeNameProvider) {
        this.topic = topic;
        this.tag = tag;
        this.producer = producer;
        this.serializeService = serializeService;
        this.typeNameProvider = typeNameProvider;
    }

    protected QueueMessage createApplicationMessage(ApplicationMessage message) {
        Assert.nonNull(topic, "topic");
        String appMessageData = serializeService.serialize(message);
        GenericApplicationMessage applicationMessage = new GenericApplicationMessage();
        applicationMessage.setApplicationMessageData(appMessageData);
        applicationMessage.setApplicationMessageType(typeNameProvider.getTypeName(message.getClass()));
        String data = serializeService.serialize(applicationMessage);
        String routeKey = message.getId();
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setBody(data);
        queueMessage.setRouteKey(routeKey);
        queueMessage.setKey(message.getId());
        queueMessage.setTopic(topic);
        queueMessage.setTag(tag);
        queueMessage.setType(MessageTypeCode.ApplicationMessage.getValue());
        return queueMessage;
    }

    @Override
    public CompletableFuture<Boolean> publishAsync(ApplicationMessage message) {
        return producer.sendMessageAsync(createApplicationMessage(message));
    }
}
