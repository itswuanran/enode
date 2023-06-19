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
    public static String MessageKey = "MessageType";

    private final Map<String, MessageHandler> messageHandlerMap;

    public PulsarMessageListener(Map<String, MessageHandler> messageHandlerMap) {
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
        String mType = messageExt.getProperty(MessageKey);
        queueMessage.setBody(value);
        queueMessage.setType(mType);
        queueMessage.setTopic(messageExt.getTopicName());
        queueMessage.setRouteKey(messageExt.getKey());
        queueMessage.setKey(new String(messageExt.getOrderingKey(), StandardCharsets.UTF_8));
        return queueMessage;
    }
}
