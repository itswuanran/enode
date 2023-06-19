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
package org.enode.redis.message;

import org.enodeframework.queue.MessageHandler;
import org.enodeframework.queue.QueueMessage;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.api.listener.ListAddListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author anruence@gmail.com
 */
public class RedisReplyMessageListener {
    private static final Logger logger = LoggerFactory.getLogger(RedisReplyMessageListener.class);
    private final String queueName;
    private final Map<String, MessageHandler> messageHandlerMap;
    private final RedissonReactiveClient redissonReactiveClient;

    public RedisReplyMessageListener(RedissonReactiveClient redissonReactiveClient, Map<String, MessageHandler> messageHandlerMap, String queueName) {
        this.redissonReactiveClient = redissonReactiveClient;
        this.messageHandlerMap = messageHandlerMap;
        this.queueName = queueName;
    }

    public void start() {
        redissonReactiveClient.getQueue(queueName).addListener(new RedisReplyMessageQueueListener(messageHandlerMap));
    }

    public void close() {
        redissonReactiveClient.shutdown();
    }

    class RedisReplyMessageQueueListener implements ListAddListener {
        public RedisReplyMessageQueueListener(Map<String, MessageHandler> messageHandlerMap) {
            this.messageHandlerMap = messageHandlerMap;
        }

        private final Map<String, MessageHandler> messageHandlerMap;

        @Override
        public void onListAdd(String name) {
            redissonReactiveClient.getQueue(queueName).poll().map(x -> {
                return (QueueMessage) x;
            }).toFuture().thenApply(y -> {
                this.received(y);
                return null;
            });
        }

        public void received(QueueMessage queueMessage) {
            MessageHandler messageHandler = messageHandlerMap.get(queueMessage.getType());
            if (messageHandler == null) {
                logger.error("No messageHandler for message: {}.", queueMessage);
                return;
            }
            messageHandler.handle(queueMessage, x -> {
            });
        }
    }
}

