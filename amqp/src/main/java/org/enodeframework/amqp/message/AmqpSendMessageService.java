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

import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.common.extensions.SysProperties;
import org.enodeframework.common.serializing.SerializeService;
import org.enodeframework.messaging.ReplyMessage;
import org.enodeframework.queue.QueueMessage;
import org.enodeframework.queue.SendMessageResult;
import org.enodeframework.queue.SendMessageService;
import org.enodeframework.queue.SendReplyService;
import org.enodeframework.queue.reply.GenericReplyMessage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AsyncAmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class AmqpSendMessageService implements SendMessageService, SendReplyService {
    private final String replyExchange;
    private final AsyncAmqpTemplate asyncAmqpTemplate;
    private final SerializeService serializeService;

    private static final Logger logger = LoggerFactory.getLogger(AmqpSendMessageService.class);

    public AmqpSendMessageService(String replyExchange, AsyncAmqpTemplate asyncAmqpTemplate, SerializeService serializeService) {
        this.replyExchange = replyExchange;
        this.asyncAmqpTemplate = asyncAmqpTemplate;
        this.serializeService = serializeService;
    }

    @Override
    public CompletableFuture<SendMessageResult> send(ReplyMessage replyMessage) {
        GenericReplyMessage message = replyMessage.asGenericReplyMessage();
        QueueMessage queueMessage = replyMessage.asPartQueueMessage();
        queueMessage.setBody(serializeService.serializeBytes(message));
        queueMessage.setTag(message.getAddress());
        queueMessage.setTopic(replyExchange);
        return sendMessageAsync(queueMessage);
    }

    private Message covertToAmqpMessage(QueueMessage queueMessage) {
        MessageProperties props = new MessageProperties();
        props.setConsumerQueue(queueMessage.channel());
        props.setConsumerTag(queueMessage.getTag());
        props.setReceivedRoutingKey(queueMessage.getRouteKey());
        props.setMessageId(queueMessage.getKey());
        props.setHeader(SysProperties.MESSAGE_TYPE_KEY, queueMessage.getType());
        props.setHeader(SysProperties.MESSAGE_TAG_KEY, queueMessage.getTag());
        return new Message(queueMessage.getBody(), props);
    }

    @NotNull
    @Override
    public CompletableFuture<SendMessageResult> sendMessageAsync(@NotNull QueueMessage queueMessage) {
        return asyncAmqpTemplate.sendAndReceive(queueMessage.getTopic(), queueMessage.getRouteKey(), this.covertToAmqpMessage(queueMessage))
            .exceptionally(throwable -> {
                logger.error("Async send message failed, error: {}, message: {}", throwable, queueMessage);
                throw new IORuntimeException(throwable);
            }).thenApply(
                x -> {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Async send message success, sendResult: {}, message: {}", x.toString(), queueMessage);
                    }
                    return new SendMessageResult(x.getMessageProperties().getMessageId(), x.getMessageProperties().getHeaders());
                });
    }
}
