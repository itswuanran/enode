package com.enodeframework.kafka;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.infrastructure.IApplicationMessage;
import com.enodeframework.queue.QueueMessage;
import com.enodeframework.queue.applicationmessage.AbstractApplicationMessagePublisher;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

public class KafkaApplicationMessagePublisher extends AbstractApplicationMessagePublisher {

    @Autowired
    private SendMessageService sendMessageService;
    private KafkaProducer<String, String> producer;

    public KafkaProducer<String, String> getProducer() {
        return producer;
    }

    public void setProducer(KafkaProducer<String, String> producer) {
        this.producer = producer;
    }

    @Override
    public CompletableFuture<AsyncTaskResult> publishAsync(IApplicationMessage message) {
        return sendMessageService.sendMessageAsync(producer, buildKafkaMessage(message));
    }

    protected ProducerRecord<String, String> buildKafkaMessage(IApplicationMessage message) {
        QueueMessage queueMessage = createApplicationMessage(message);
        return KafkaTool.covertToProducerRecord(queueMessage);
    }
}
