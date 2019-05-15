package com.enodeframework.queue.publishableexceptions;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.serializing.IJsonSerializer;
import com.enodeframework.infrastructure.IMessagePublisher;
import com.enodeframework.infrastructure.IPublishableException;
import com.enodeframework.infrastructure.ISequenceMessage;
import com.enodeframework.queue.QueueMessage;
import com.enodeframework.queue.QueueMessageTypeCode;
import com.enodeframework.queue.TopicData;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class PublishableExceptionPublisher implements IMessagePublisher<IPublishableException> {

    @Autowired
    protected IJsonSerializer jsonSerializer;


    protected TopicData topicData;

    public TopicData getTopicData() {
        return topicData;
    }

    public void setTopicData(TopicData topicData) {
        this.topicData = topicData;
    }

    @Override
    public CompletableFuture<AsyncTaskResult> publishAsync(IPublishableException exception) {
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }

    protected QueueMessage createExecptionMessage(IPublishableException exception) {
        Map<String, String> serializableInfo = new HashMap<>();
        exception.serializeTo(serializableInfo);
        ISequenceMessage sequenceMessage = null;
        if (exception instanceof ISequenceMessage) {
            sequenceMessage = (ISequenceMessage) exception;
        }
        PublishableExceptionMessage publishableExceptionMessage = new PublishableExceptionMessage();
        publishableExceptionMessage.setUniqueId(exception.id());
        publishableExceptionMessage.setAggregateRootTypeName(sequenceMessage != null ? sequenceMessage.aggregateRootTypeName() : null);
        publishableExceptionMessage.setAggregateRootId(sequenceMessage != null ? sequenceMessage.aggregateRootStringId() : null);
        publishableExceptionMessage.setExceptionType(exception.getClass().getName());
        publishableExceptionMessage.setTimestamp(exception.timestamp());
        publishableExceptionMessage.setSerializableInfo(serializableInfo);
        String data = jsonSerializer.serialize(publishableExceptionMessage);
        String routeKey = exception.getRoutingKey() == null ? exception.getRoutingKey() : exception.id();
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setCode(QueueMessageTypeCode.ExceptionMessage.getValue());
        queueMessage.setTopic(topicData.getTopic());
        queueMessage.setTags(topicData.getTag());
        queueMessage.setBody(data);
        queueMessage.setKey(exception.id());
        queueMessage.setRouteKey(routeKey);
        return queueMessage;
    }

}
