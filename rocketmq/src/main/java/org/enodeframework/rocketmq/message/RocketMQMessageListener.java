package org.enodeframework.rocketmq.message;

import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.enodeframework.common.io.Task;
import org.enodeframework.queue.IMessageHandler;
import org.enodeframework.queue.QueueMessage;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author anruence@gmail.com
 */
public class RocketMQMessageListener implements MessageListenerConcurrently {

    private final IMessageHandler messageHandler;

    public RocketMQMessageListener(IMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        CountDownLatch latch = new CountDownLatch(msgs.size());
        msgs.forEach(msg -> {
            QueueMessage queueMessage = this.covertToQueueMessage(msg);
            messageHandler.handle(queueMessage, message -> {
                latch.countDown();
            });
        });
        Task.await(latch);
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

    private QueueMessage covertToQueueMessage(MessageExt messageExt) {
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setBody(new String(messageExt.getBody(), StandardCharsets.UTF_8));
        queueMessage.setTopic(messageExt.getTopic());
        queueMessage.setTag(messageExt.getTags());
        queueMessage.setKey(messageExt.getKeys());
        return queueMessage;
    }
}