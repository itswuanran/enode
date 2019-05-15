package com.enodeframework.kafka;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.eventing.DomainEventStreamMessage;
import com.enodeframework.queue.QueueMessage;
import com.enodeframework.queue.domainevent.DomainEventPublisher;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

public class KafkaDomainEventPublisher extends DomainEventPublisher {

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
    public CompletableFuture<AsyncTaskResult> publishAsync(DomainEventStreamMessage eventStream) {
        return sendMessageService.sendMessageAsync(producer, buildKafkaMessage(eventStream));
    }

    protected ProducerRecord<String, String> buildKafkaMessage(DomainEventStreamMessage eventStream) {
        QueueMessage queueMessage = createDomainEventStreamMessage(eventStream);
        return KafkaTool.covertToProducerRecord(queueMessage);
    }

}
