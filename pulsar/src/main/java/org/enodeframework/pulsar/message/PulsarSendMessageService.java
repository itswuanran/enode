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

import org.apache.pulsar.client.api.Producer;
import org.enodeframework.common.exception.EnodeException;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.common.extensions.SysProperties;
import org.enodeframework.common.serializing.SerializeService;
import org.enodeframework.messaging.ReplyMessage;
import org.enodeframework.queue.MessageTypeCode;
import org.enodeframework.queue.QueueMessage;
import org.enodeframework.queue.SendMessageResult;
import org.enodeframework.queue.SendMessageService;
import org.enodeframework.queue.SendReplyService;
import org.enodeframework.queue.reply.GenericReplyMessage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class PulsarSendMessageService implements SendMessageService, SendReplyService {

    private static final Logger logger = LoggerFactory.getLogger(PulsarSendMessageService.class);

    private final Map<String, Producer<byte[]>> producerMap;

    private final SerializeService serializeService;

    public PulsarSendMessageService(Map<String, Producer<byte[]>> producerMap, SerializeService serializeService) {
        this.producerMap = producerMap;
        this.serializeService = serializeService;
    }

    @Override
    public CompletableFuture<SendMessageResult> sendMessageAsync(QueueMessage queueMessage) {
        Producer<byte[]> producer = producerMap.get(queueMessage.getType());
        return producer.newMessage()
            .key(queueMessage.getRouteKey())
            .property(SysProperties.MESSAGE_TYPE_KEY, queueMessage.getType())
            .property(SysProperties.MESSAGE_TAG_KEY, queueMessage.getTag())
            .properties(queueMessage.getItems())
            .value(queueMessage.getBody())
            .orderingKey(queueMessage.getKey().getBytes(StandardCharsets.UTF_8))
            .sendAsync()
            .exceptionally(throwable -> {
                logger.error("Async send message has exception, message: {}", queueMessage, throwable);
                throw new IORuntimeException(throwable);
            })
            .thenApply((messageId) -> {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                        "Async send message success, sendResult: {}, message: {}", messageId, queueMessage);
                }
                return new SendMessageResult(new String(messageId.toByteArray(), StandardCharsets.UTF_8));
            });
    }

    @NotNull
    @Override
    public CompletableFuture<SendMessageResult> send(@NotNull ReplyMessage message) {
        return sendMessageAsync(buildQueueMessage(message));
    }

    private Producer<byte[]> chooseProducer(String pType) {
        Producer<byte[]> producer = producerMap.get(pType);
        if (producer == null) {
            String msg = String.format("No producer for type: [%s]", pType);
            throw new EnodeException(msg);
        }
        return producer;
    }

    private QueueMessage buildQueueMessage(ReplyMessage replyMessage) {
        Producer<byte[]> producer = chooseProducer(MessageTypeCode.ReplyMessage.getValue());
        GenericReplyMessage message = replyMessage.asGenericReplyMessage();
        QueueMessage queueMessage = replyMessage.asPartQueueMessage();
        queueMessage.setTopic(producer.getTopic());
        queueMessage.setBody(serializeService.serializeBytes(message));
        queueMessage.setType(MessageTypeCode.ReplyMessage.getValue());
        return queueMessage;
    }
}
