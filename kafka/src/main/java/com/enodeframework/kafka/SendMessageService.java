package com.enodeframework.kafka;

import com.enodeframework.common.io.AsyncTaskResult;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class SendMessageService {

    private static Logger logger = LoggerFactory.getLogger(SendMessageService.class);

    public CompletableFuture<AsyncTaskResult> sendMessageAsync(Producer<String, String> producer, ProducerRecord<String, String> record) {
        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                logger.error("send message failed. topic:{}, metadata:{}", metadata.topic(), metadata.toString(), exception);
            }
        });
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }
}
