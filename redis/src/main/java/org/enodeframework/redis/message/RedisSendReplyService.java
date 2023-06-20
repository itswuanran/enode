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

import org.enodeframework.common.extensions.SysProperties;
import org.enodeframework.common.serializing.SerializeService;
import org.enodeframework.messaging.ReplyMessage;
import org.enodeframework.queue.SendMessageResult;
import org.enodeframework.queue.SendReplyService;
import org.enodeframework.queue.reply.GenericReplyMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class RedisSendReplyService implements SendReplyService {
    private final String channel;
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final SerializeService serializeService;

    private static final Logger logger = LoggerFactory.getLogger(RedisSendReplyService.class);

    public RedisSendReplyService(String channel, ReactiveRedisTemplate<String, String> reactiveRedisTemplate, SerializeService serializeService) {
        this.channel = channel;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.serializeService = serializeService;
    }

    @Override
    public CompletableFuture<SendMessageResult> send(ReplyMessage message) {
        GenericReplyMessage genericReplyMessage = message.asGenericReplyMessage();
        String destination = String.format(SysProperties.CHANNEL_TAG_TPL, channel, message.getAddress());
        return reactiveRedisTemplate.convertAndSend(destination, serializeService.serialize(genericReplyMessage))
            .toFuture()
            .thenApply(x -> {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                        "Async send message success, sendResult: {}, message: {}", x, genericReplyMessage);
                }
                return new SendMessageResult(String.valueOf(x));
            });
    }
}
