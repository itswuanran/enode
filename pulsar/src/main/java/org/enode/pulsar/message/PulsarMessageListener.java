package org.enode.pulsar.message;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageListener;
import org.apache.pulsar.client.api.PulsarClientException;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.queue.IMessageHandler;
import org.enodeframework.queue.QueueMessage;

import java.nio.charset.StandardCharsets;

/**
 * @author anruence@gmail.com
 */
public class PulsarMessageListener implements MessageListener<byte[]> {

    private final IMessageHandler messageHandler;

    public PulsarMessageListener(IMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public void received(Consumer<byte[]> consumer, Message<byte[]> msg) {
        QueueMessage queueMessage = this.toQueueMessage(msg);
        messageHandler.handle(queueMessage, x -> {
            try {
                consumer.acknowledge(msg);
            } catch (PulsarClientException e) {
                throw new IORuntimeException(e);
            }
        });
    }

    private QueueMessage toQueueMessage(Message<byte[]> messageExt) {
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setBody(new String(messageExt.getValue(), StandardCharsets.UTF_8));
        queueMessage.setTopic(messageExt.getTopicName());
        queueMessage.setRouteKey(messageExt.getKey());
        queueMessage.setKey(new String(messageExt.getOrderingKey(), StandardCharsets.UTF_8));
        return queueMessage;
    }
}