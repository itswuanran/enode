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

import org.enodeframework.commanding.CommandOptions;
import org.enodeframework.common.serializing.SerializeService;
import org.enodeframework.messaging.ReplyMessage;
import org.enodeframework.queue.SendMessageResult;
import org.enodeframework.queue.SendReplyService;
import org.enodeframework.queue.reply.GenericReplyMessage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class RedisSendReplyService implements SendReplyService {
    private final CommandOptions commandOptions;
    private final ReactiveStringRedisTemplate reactiveRedisTemplate;
    private final SerializeService serializeService;
    private final Logger logger = LoggerFactory.getLogger(RedisSendReplyService.class);

    public RedisSendReplyService(CommandOptions commandOptions, ReactiveStringRedisTemplate reactiveRedisTemplate, SerializeService serializeService) {
        this.commandOptions = commandOptions;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.serializeService = serializeService;
    }

    @NotNull
    @Override
    public CompletableFuture<SendMessageResult> send(ReplyMessage message) {
        GenericReplyMessage genericReplyMessage = message.asGenericReplyMessage();
        String destination = commandOptions.replyWith(message.getAddress());
        return reactiveRedisTemplate.convertAndSend(destination, serializeService.serialize(genericReplyMessage))
            .toFuture()
            .thenApply(x -> {
                if (logger.isDebugEnabled()) {
                    logger.debug("Async send message success, sendResult: {}, message: {}", x, genericReplyMessage);
                }
                return new SendMessageResult(String.valueOf(x));
            });
    }
}
