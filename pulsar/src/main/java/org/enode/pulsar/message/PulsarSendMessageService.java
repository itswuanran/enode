package org.enode.pulsar.message;

import org.apache.pulsar.client.api.Producer;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.common.utils.Assert;
import org.enodeframework.queue.QueueMessage;
import org.enodeframework.queue.SendMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author anruence@gmail.com
 */
public class PulsarSendMessageService implements SendMessageService {

    private static final Logger logger = LoggerFactory.getLogger(PulsarSendMessageService.class);

    private final Map<String, Producer<byte[]>> producerMap;

    public PulsarSendMessageService(List<Producer<byte[]>> producers) {
        Assert.nonEmpty(producers, "Pulsar producers");
        this.producerMap = producers.stream().collect(Collectors.toMap(Producer::getTopic, producer -> producer));
    }

    @Override
    public CompletableFuture<Boolean> sendMessageAsync(QueueMessage queueMessage) {
        Producer<byte[]> producer = producerMap.get(queueMessage.getTopic());
        if (producer == null) {
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            future.completeExceptionally(new IORuntimeException(String.format("No producer for topic: [%s]", queueMessage.getTopic())));
            logger.error("No pulsar producer for topic [{}]", queueMessage.getTopic());
            return future;
        }
        return producer.newMessage()
            .key(queueMessage.getRouteKey())
            .value(queueMessage.getBodyAndType().getBytes())
            .orderingKey(queueMessage.getKey().getBytes())
            .sendAsync()
            .exceptionally(throwable -> {
                logger.error("Async send message has exception, message: {}", queueMessage, throwable);
                throw new IORuntimeException(throwable);
            })
            .thenApply(messageId -> {
                if (logger.isDebugEnabled()) {
                    logger.debug("Async send message success, sendResult: {}, message: {}", messageId, queueMessage);
                }
                return true;
            });
    }
}
