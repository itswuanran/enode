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
package org.enodeframework.amqp.message;

import com.rabbitmq.client.Channel;
import org.enodeframework.common.extensions.SysProperties;
import org.enodeframework.common.io.Task;
import org.enodeframework.queue.MessageHandlerHolder;
import org.enodeframework.queue.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareBatchMessageListener;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author anruence@gmail.com
 */
public class AmqpBatchMessageListener implements ChannelAwareBatchMessageListener {
    private final MessageHandlerHolder messageHandlerMap;
    private final Logger logger = LoggerFactory.getLogger(AmqpBatchMessageListener.class);

    public AmqpBatchMessageListener(MessageHandlerHolder messageHandlerMap) {
        this.messageHandlerMap = messageHandlerMap;
    }

    @Override
    public void onMessage(Message message, Channel channel) {
        QueueMessage queueMessage = this.covertToQueueMessage(message);
        messageHandlerMap.chooseMessageHandler(queueMessage.getType()).handle(queueMessage, context -> {
            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                logger.error("Acknowledge message failed: {}.", message, e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void onMessageBatch(List<Message> messages, Channel channel) {
        CountDownLatch latch = new CountDownLatch(messages.size());
        messages.forEach(message -> {
            QueueMessage queueMessage = this.covertToQueueMessage(message);
            messageHandlerMap.chooseMessageHandler(queueMessage.getType()).handle(queueMessage, context -> {
                latch.countDown();
                try {
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
        Task.await(latch);
    }

    private QueueMessage covertToQueueMessage(Message record) {
        MessageProperties props = record.getMessageProperties();
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setBody(record.getBody());
        queueMessage.setTag(props.getConsumerTag());
        queueMessage.setKey(props.getMessageId());
        queueMessage.setTopic(props.getConsumerQueue());
        queueMessage.setType(props.getHeader(SysProperties.MESSAGE_TYPE_KEY));
        return queueMessage;
    }
}
