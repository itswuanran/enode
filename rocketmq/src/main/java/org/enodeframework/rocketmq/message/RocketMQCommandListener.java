package org.enodeframework.rocketmq.message;

import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.message.MessageExt;
import org.enodeframework.queue.IMessageHandler;

import java.util.List;

/**
 * @author anruence@gmail.com
 */
public class RocketMQCommandListener implements MessageListenerOrderly {

    private final IMessageHandler commandListener;

    public RocketMQCommandListener(IMessageHandler commandListener) {
        this.commandListener = commandListener;
    }

    @Override
    public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
        RocketMQTool.handle(msgs, commandListener);
        return ConsumeOrderlyStatus.SUCCESS;
    }

    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        RocketMQTool.handle(msgs, commandListener);
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}