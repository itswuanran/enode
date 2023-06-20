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
package org.enodeframework.ons.message;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.OnExceptionContext;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.SendCallback;
import com.aliyun.openservices.ons.api.SendResult;
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

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class OnsSendMessageService implements SendMessageService, SendReplyService {

    private static final Logger logger = LoggerFactory.getLogger(OnsSendMessageService.class);
    private final String replyTopic;
    private final Producer producer;
    private final SerializeService serializeService;

    public OnsSendMessageService(String replyTopic, Producer producer, SerializeService serializeService) {
        this.replyTopic = replyTopic;
        this.producer = producer;
        this.serializeService = serializeService;
    }

    @Override
    public CompletableFuture<SendMessageResult> sendMessageAsync(QueueMessage queueMessage) {
        CompletableFuture<SendMessageResult> future = new CompletableFuture<>();
        Message message = this.covertToProducerRecord(queueMessage);
        producer.sendAsync(message, new SendCallback() {
            @Override
            public void onSuccess(SendResult result) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Async send message success, sendResult: {}, message: {}", result, message);
                }
                future.complete(new SendMessageResult(result.getMessageId()));
            }

            @Override
            public void onException(OnExceptionContext onExceptionContext) {
                future.completeExceptionally(new IORuntimeException(onExceptionContext.getException()));
                logger.error("Async send message has exception, message: {}, routingKey: {}", message, message.getShardingKey(), onExceptionContext.getException());
            }
        });
        return future;
    }

    private QueueMessage buildQueueMessage(ReplyMessage replyMessage) {
        GenericReplyMessage message = replyMessage.asGenericReplyMessage();
        QueueMessage queueMessage = replyMessage.asPartQueueMessage();
        queueMessage.setTopic(replyTopic);
        queueMessage.setBody(serializeService.serializeBytes(message));
        queueMessage.setType(MessageTypeCode.ReplyMessage.getValue());
        return queueMessage;
    }

    private Message covertToProducerRecord(QueueMessage queueMessage) {
        Message message = new Message(queueMessage.getTopic(), queueMessage.getTag(), queueMessage.getKey(), queueMessage.getBody());
        message.setShardingKey(queueMessage.getRouteKey());
        message.putUserProperties(SysProperties.MESSAGE_TYPE_KEY, queueMessage.getType());
        return message;
    }

    @NotNull
    @Override
    public CompletableFuture<SendMessageResult> send(@NotNull ReplyMessage message) {
        return sendMessageAsync(buildQueueMessage(message));
    }
}
