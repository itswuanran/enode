package org.enodeframework.rocketmq.message;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.selector.SelectMessageQueueByHash;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.queue.QueueMessage;
import org.enodeframework.queue.SendMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class RocketMQSendMessageService implements SendMessageService {

    private static final Logger logger = LoggerFactory.getLogger(RocketMQSendMessageService.class);

    private final MQProducer producer;

    public RocketMQSendMessageService(MQProducer producer) {
        this.producer = producer;
    }

    @Override
    public CompletableFuture<Boolean> sendMessageAsync(QueueMessage queueMessage) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Message message = this.covertToProducerRecord(queueMessage);
        try {
            producer.send(message, new SelectMessageQueueByHash(), queueMessage.getRouteKey(), new SendCallback() {
                @Override
                public void onSuccess(SendResult result) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Async send message success, sendResult: {}, message: {}", result, queueMessage);
                    }
                    future.complete(true);
                }

                @Override
                public void onException(Throwable ex) {
                    future.completeExceptionally(new IORuntimeException(ex));
                    logger.error("Async send message has exception, message: {}", queueMessage, ex);
                }
            });
        } catch (MQClientException | RemotingException | InterruptedException ex) {
            future.completeExceptionally(new IORuntimeException(ex));
            logger.error("Async send message has exception, message: {}", queueMessage, ex);
        }
        return future;
    }

    private Message covertToProducerRecord(QueueMessage queueMessage) {
        Message message = new Message(queueMessage.getTopic(), queueMessage.getTag(), queueMessage.getKey(), queueMessage.getBodyAndType().getBytes(StandardCharsets.UTF_8));
        return message;
    }
}
