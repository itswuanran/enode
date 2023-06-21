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
package org.enodeframework.redis.message;

import org.enodeframework.queue.MessageHandlerHolder;
import org.enodeframework.queue.MessageTypeCode;
import org.enodeframework.queue.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import java.nio.charset.StandardCharsets;

/**
 * @author anruence@gmail.com
 */
public class RedisReplyMessageListener implements MessageListener {
    private static final Logger logger = LoggerFactory.getLogger(RedisReplyMessageListener.class);
    private final MessageHandlerHolder messageHandlerHolder;

    public RedisReplyMessageListener(MessageHandlerHolder messageHandlerHolder) {
        this.messageHandlerHolder = messageHandlerHolder;
    }

    public QueueMessage convertQueueMessage(Message message) {
        QueueMessage queueMessage = new QueueMessage();
        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
        String[] topicTag = channel.split("#");
        queueMessage.setType(MessageTypeCode.ReplyMessage.getValue());
        queueMessage.setTopic(channel);
        if (topicTag.length >= 2) {
            queueMessage.setTag(topicTag[1]);
            queueMessage.setTopic(topicTag[0]);
        }
        queueMessage.setBody(message.getBody());
        return queueMessage;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        QueueMessage queueMessage = convertQueueMessage(message);
        messageHandlerHolder.chooseMessageHandler(queueMessage.getType()).handle(queueMessage, x -> {
        });
    }
}

