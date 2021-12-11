package org.enodeframework.rocketmq.message;

import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.enodeframework.common.io.Task;
import org.enodeframework.queue.MessageHandler;
import org.enodeframework.queue.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @author anruence@gmail.com
 */
public class RocketMQMessageListener implements MessageListenerConcurrently {

    private static final Logger logger = LoggerFactory.getLogger(RocketMQMessageListener.class);

    private final Map<Character, MessageHandler> messageHandlerMap;

    public RocketMQMessageListener(Map<Character, MessageHandler> messageHandlerMap) {
        this.messageHandlerMap = messageHandlerMap;
    }

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        CountDownLatch latch = new CountDownLatch(msgs.size());
        msgs.forEach(msg -> {
            QueueMessage queueMessage = this.covertToQueueMessage(msg);
            MessageHandler messageHandler = messageHandlerMap.get(queueMessage.getType());
            if (messageHandler == null) {
                logger.error("No messageHandler for message: {}.", queueMessage);
                latch.countDown();
                return;
            }
            messageHandler.handle(queueMessage, message -> {
                latch.countDown();
            });
        });
        Task.await(latch);
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

    private QueueMessage covertToQueueMessage(MessageExt messageExt) {
        QueueMessage queueMessage = new QueueMessage();
        String value = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        int length = value.length();
        queueMessage.setBody(value.substring(0, length - 2));
        queueMessage.setType(value.charAt(length - 1));
        queueMessage.setTopic(messageExt.getTopic());
        queueMessage.setTag(messageExt.getTags());
        queueMessage.setKey(messageExt.getKeys());
        return queueMessage;
    }
}