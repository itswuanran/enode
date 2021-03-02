package org.enodeframework.rocketmq.message;

import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
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
public class RocketMQMessageOrderListener implements MessageListenerOrderly {

    private final IMessageHandler messageHandler;

    public RocketMQMessageOrderListener(IMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
        CountDownLatch latch = new CountDownLatch(msgs.size());
        msgs.forEach(msg -> {
            QueueMessage queueMessage = this.covertToQueueMessage(msg);
            messageHandler.handle(queueMessage, message -> {
                latch.countDown();
            });
        });
        Task.await(latch);
        return ConsumeOrderlyStatus.SUCCESS;
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