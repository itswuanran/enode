package org.enodeframework.queue.publishableexceptions;

import org.enodeframework.common.serializing.SerializeService;
import org.enodeframework.common.utils.Assert;
import org.enodeframework.domain.DomainExceptionMessage;
import org.enodeframework.messaging.MessagePublisher;
import org.enodeframework.queue.QueueMessage;
import org.enodeframework.queue.SendMessageService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DefaultPublishableExceptionPublisher implements MessagePublisher<DomainExceptionMessage> {

    private final String topic;

    private final String tag;

    private final SendMessageService sendMessageService;

    private final SerializeService serializeService;

    public DefaultPublishableExceptionPublisher(String topic, String tag, SendMessageService sendMessageService, SerializeService serializeService) {
        this.topic = topic;
        this.tag = tag;
        this.sendMessageService = sendMessageService;
        this.serializeService = serializeService;
    }

    protected QueueMessage createExceptionMessage(DomainExceptionMessage exception) {
        Assert.nonNull(topic, "topic");
        Map<String, Object> serializableInfo = new HashMap<>();
        exception.serializeTo(serializableInfo);
        GenericPublishableExceptionMessage exceptionMessage = new GenericPublishableExceptionMessage();
        exceptionMessage.setUniqueId(exception.getId());
        exceptionMessage.setExceptionType(exception.getClass().getName());
        exceptionMessage.setTimestamp(exception.getTimestamp());
        exceptionMessage.setSerializableInfo(serializableInfo);
        exceptionMessage.setItems(exception.getItems());
        String data = serializeService.serialize(exceptionMessage);
        String routeKey = exception.getId();
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setTopic(topic);
        queueMessage.setTag(tag);
        queueMessage.setBody(data);
        queueMessage.setRouteKey(routeKey);
        queueMessage.setKey(exceptionMessage.getUniqueId());
        return queueMessage;
    }

    @Override
    public CompletableFuture<Boolean> publishAsync(DomainExceptionMessage message) {
        return sendMessageService.sendMessageAsync(createExceptionMessage(message));
    }
}
