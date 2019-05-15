package com.enodeframework.queue.applicationmessage;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.serializing.IJsonSerializer;
import com.enodeframework.infrastructure.IApplicationMessage;
import com.enodeframework.infrastructure.IMessagePublisher;
import com.enodeframework.queue.QueueMessage;
import com.enodeframework.queue.QueueMessageTypeCode;
import com.enodeframework.queue.TopicData;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

public abstract class ApplicationMessagePublisher implements IMessagePublisher<IApplicationMessage> {

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
    public CompletableFuture<AsyncTaskResult> publishAsync(IApplicationMessage message) {
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }

    protected QueueMessage createApplicationMessage(IApplicationMessage message) {
        String appMessageData = jsonSerializer.serialize(message);
        ApplicationDataMessage appDataMessage = new ApplicationDataMessage(appMessageData, message.getClass().getName());
        String data = jsonSerializer.serialize(appDataMessage);
        String routeKey = message.getRoutingKey() != null ? message.getRoutingKey() : message.id();
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setBody(data);
        queueMessage.setRouteKey(routeKey);
        queueMessage.setCode(QueueMessageTypeCode.ApplicationMessage.getValue());
        queueMessage.setKey(message.id());
        queueMessage.setTopic(topicData.getTopic());
        queueMessage.setTags(topicData.getTag());
        return queueMessage;
    }
}
