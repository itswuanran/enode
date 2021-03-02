package org.enode.pulsar.message;

import org.apache.pulsar.client.api.Producer;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.queue.ISendMessageService;
import org.enodeframework.queue.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author anruence@gmail.com
 */
public class PulsarSendMessageService implements ISendMessageService {

    private static final Logger logger = LoggerFactory.getLogger(PulsarSendMessageService.class);

    private final Map<String, Producer<byte[]>> producers;

    public PulsarSendMessageService(List<Producer<byte[]>> producers) {
        this.producers = Optional.ofNullable(producers).orElse(new ArrayList<>())
                .stream().collect(Collectors.toMap(Producer::getTopic, y -> y));
        if (this.producers.isEmpty()) {
            throw new IllegalArgumentException("producers can not empty.");
        }
    }

    @Override
    public CompletableFuture<Boolean> sendMessageAsync(QueueMessage queueMessage) {
        Producer<byte[]> producer = producers.get(queueMessage.getTopic());
        if (producer == null) {
            logger.error("can not find pulsar producer for topic [{}]", queueMessage.getTopic());
            return CompletableFuture.completedFuture(false);
        }
        return producer.newMessage()
                .key(queueMessage.getRouteKey())
                .value(queueMessage.getBody().getBytes())
                .orderingKey(queueMessage.getKey().getBytes())
                .sendAsync()
                .exceptionally(throwable -> {
                    logger.error("Enode message async send has exception, message: {}, routingKey: {}", queueMessage.getBody(), queueMessage.getRouteKey(), throwable);
                    throw new IORuntimeException(throwable);
                })
                .thenApply(x -> {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Enode message async send success, sendResult: {}, message: {}", x, queueMessage.getBody());
                    }
                    return true;
                });
    }
}
