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
package org.enodeframework.pulsar.message;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageListener;
import org.apache.pulsar.client.api.PulsarClientException;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.common.extensions.SysProperties;
import org.enodeframework.queue.MessageHandler;
import org.enodeframework.queue.MessageHandlerHolder;
import org.enodeframework.queue.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * @author anruence@gmail.com
 */
public class PulsarMessageListener implements MessageListener<byte[]> {

    private static final Logger logger = LoggerFactory.getLogger(PulsarMessageListener.class);

    private final MessageHandlerHolder messageHandlerHolder;

    public PulsarMessageListener(MessageHandlerHolder messageHandlerHolder) {
        this.messageHandlerHolder = messageHandlerHolder;
    }

    @Override
    public void received(Consumer<byte[]> consumer, Message<byte[]> msg) {
        QueueMessage queueMessage = this.toQueueMessage(msg);
        MessageHandler messageHandler = messageHandlerHolder.chooseMessageHandle(queueMessage.getType());
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
        String mType = messageExt.getProperty(SysProperties.MESSAGE_TYPE_KEY);
        String tag = messageExt.getProperty(SysProperties.MESSAGE_TAG_KEY);
        queueMessage.setBody(messageExt.getValue());
        queueMessage.setType(mType);
        queueMessage.setTag(tag);
        queueMessage.setTopic(messageExt.getTopicName());
        queueMessage.setRouteKey(messageExt.getKey());
        queueMessage.setKey(new String(messageExt.getOrderingKey(), StandardCharsets.UTF_8));
        return queueMessage;
    }
}
