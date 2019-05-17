package com.enodeframework.kafka;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.infrastructure.IPublishableException;
import com.enodeframework.queue.QueueMessage;
import com.enodeframework.queue.publishableexceptions.AbstractPublishableExceptionPublisher;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

public class KafkaPublishableExceptionPublisher extends AbstractPublishableExceptionPublisher {

    @Autowired
    protected SendMessageService sendMessageService;
    private KafkaProducer<String, String> producer;

    public KafkaProducer<String, String> getProducer() {
        return producer;
    }

    public void setProducer(KafkaProducer<String, String> producer) {
        this.producer = producer;
    }

    @Override
    public CompletableFuture<AsyncTaskResult> publishAsync(IPublishableException exception) {
        return sendMessageService.sendMessageAsync(producer, buildKafkaMessage(exception));
    }

    protected ProducerRecord<String, String> buildKafkaMessage(IPublishableException exception) {
        QueueMessage queueMessage = createExecptionMessage(exception);
        return KafkaTool.covertToProducerRecord(queueMessage);
    }
}
