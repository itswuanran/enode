package com.enode.kafka;

import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IApplicationMessage;
import com.enode.queue.QueueMessage;
import com.enode.queue.applicationmessage.ApplicationMessagePublisher;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

public class KafkaApplicationMessagePublisher extends ApplicationMessagePublisher {

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
