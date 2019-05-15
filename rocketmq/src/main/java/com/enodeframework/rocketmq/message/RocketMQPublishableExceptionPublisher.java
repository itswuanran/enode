package com.enodeframework.rocketmq.message;

import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.common.message.Message;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.infrastructure.IPublishableException;
import com.enodeframework.queue.QueueMessage;
import com.enodeframework.queue.publishableexceptions.PublishableExceptionPublisher;

import java.util.concurrent.CompletableFuture;

public class RocketMQPublishableExceptionPublisher extends PublishableExceptionPublisher {

    private DefaultMQProducer producer;

    public DefaultMQProducer getProducer() {
        return producer;
    }

    public void setProducer(DefaultMQProducer producer) {
        this.producer = producer;
    }

    @Override
    public CompletableFuture<AsyncTaskResult> publishAsync(IPublishableException exception) {
        QueueMessage queueMessage = createExecptionMessage(exception);
        Message message = RocketMQTool.covertToProducerRecord(queueMessage);
        return SendRocketMQService.sendMessageAsync(producer, message, queueMessage.getRouteKey());
    }
}
