package org.enode.pulsar.message;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageListener;
import org.apache.pulsar.client.api.PulsarClientException;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.queue.MessageHandler;
import org.enodeframework.queue.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author anruence@gmail.com
 */
public class PulsarMessageListener implements MessageListener<byte[]> {

    private static final Logger logger = LoggerFactory.getLogger(PulsarMessageListener.class);

    private final Map<Character, MessageHandler> messageHandlerMap;

    public PulsarMessageListener(Map<Character, MessageHandler> messageHandlerMap) {
        this.messageHandlerMap = messageHandlerMap;
    }

    @Override
    public void received(Consumer<byte[]> consumer, Message<byte[]> msg) {
        QueueMessage queueMessage = this.toQueueMessage(msg);
        MessageHandler messageHandler = messageHandlerMap.get(queueMessage.getType());
        if (messageHandler == null) {
            logger.error("No messageHandler for message: {}.", queueMessage);
            return;
        }
        messageHandler.handle(queueMessage, x -> {
            try {
                consumer.acknowledge(msg);
            } catch (PulsarClientException e) {
                logger.error("Acknowledge message fail: {}.", queueMessage, e);
                throw new IORuntimeException(e);
            }
        });
    }

    private QueueMessage toQueueMessage(Message<byte[]> messageExt) {
        QueueMessage queueMessage = new QueueMessage();
        String value = new String(messageExt.getValue(), StandardCharsets.UTF_8);
        int length = value.length();
        // 格式为{}|1
        queueMessage.setBody(value.substring(0, length - 2));
        queueMessage.setType(value.charAt(length - 1));
        queueMessage.setTopic(messageExt.getTopicName());
        queueMessage.setRouteKey(messageExt.getKey());
        queueMessage.setKey(new String(messageExt.getOrderingKey(), StandardCharsets.UTF_8));
        return queueMessage;
    }
}