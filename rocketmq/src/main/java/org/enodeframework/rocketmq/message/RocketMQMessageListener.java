/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.enodeframework.rocketmq.message;

import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.message.MessageExt;
import org.enodeframework.common.extensions.SysProperties;
import org.enodeframework.common.io.Task;
import org.enodeframework.queue.MessageHandler;
import org.enodeframework.queue.MessageHandlerHolder;
import org.enodeframework.queue.QueueMessage;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author anruence@gmail.com
 */
public class RocketMQMessageListener implements MessageListenerConcurrently, MessageListenerOrderly {

    private final MessageHandlerHolder messageHandlerHolder;

    public RocketMQMessageListener(MessageHandlerHolder messageHandlerHolder) {
        this.messageHandlerHolder = messageHandlerHolder;
    }

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        CountDownLatch latch = new CountDownLatch(msgs.size());
        msgs.forEach(msg -> {
            QueueMessage queueMessage = this.covertToQueueMessage(msg);
            MessageHandler messageHandler = messageHandlerHolder.chooseMessageHandle(queueMessage.getType());
            messageHandler.handle(queueMessage, message -> {
                latch.countDown();
            });
        });
        Task.await(latch);
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

    private QueueMessage covertToQueueMessage(MessageExt messageExt) {
        QueueMessage queueMessage = new QueueMessage();
        String mType = messageExt.getUserProperty(SysProperties.MESSAGE_TYPE_KEY);
        queueMessage.setBody(messageExt.getBody());
        queueMessage.setType(mType);
        queueMessage.setTopic(messageExt.getTopic());
        queueMessage.setTag(messageExt.getTags());
        queueMessage.setKey(messageExt.getKeys());
        return queueMessage;
    }

    @Override
    public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
        CountDownLatch latch = new CountDownLatch(msgs.size());
        msgs.forEach(msg -> {
            QueueMessage queueMessage = this.covertToQueueMessage(msg);
            MessageHandler messageHandler = messageHandlerHolder.chooseMessageHandle(queueMessage.getType());
            messageHandler.handle(queueMessage, message -> {
                latch.countDown();
            });
        });
        Task.await(latch);
        return ConsumeOrderlyStatus.SUCCESS;
    }
}
