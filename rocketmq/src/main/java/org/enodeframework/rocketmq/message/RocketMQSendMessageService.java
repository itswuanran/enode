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
package org.enodeframework.rocketmq.message;

import com.google.common.collect.Maps;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.selector.SelectMessageQueueByHash;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class RocketMQSendMessageService implements SendMessageService, SendReplyService {

    private final Logger logger = LoggerFactory.getLogger(RocketMQSendMessageService.class);
    private final String replyTopic;
    private final MQProducer producer;
    private final SerializeService serializeService;

    public RocketMQSendMessageService(String replyTopic, MQProducer producer, SerializeService serializeService) {
        this.replyTopic = replyTopic;
        this.producer = producer;
        this.serializeService = serializeService;
    }

    @Override
    public CompletableFuture<SendMessageResult> sendMessageAsync(QueueMessage queueMessage) {
        CompletableFuture<SendMessageResult> future = new CompletableFuture<>();
        Message message = this.covertToProducerRecord(queueMessage);
        try {
            producer.send(message, new SelectMessageQueueByHash(), queueMessage.getRouteKey(), new SendCallback() {
                @Override
                public void onSuccess(SendResult result) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Async send message success, sendResult: {}, message: {}", result, queueMessage);
                    }
                    Map<String, Object> items = Maps.newHashMap();
                    items.put("result", result);
                    future.complete(new SendMessageResult(result.getMsgId(), items));
                }

                @Override
                public void onException(Throwable ex) {
                    future.completeExceptionally(new IORuntimeException(ex));
                    logger.error("Async send message has exception, message: {}", queueMessage, ex);
                }
            });
        } catch (MQClientException | RemotingException | InterruptedException ex) {
            future.completeExceptionally(new IORuntimeException(ex));
            logger.error("Async send message has exception, message: {}", queueMessage, ex);
        }
        return future;
    }

    @Override
    public CompletableFuture<SendMessageResult> send(ReplyMessage message) {
        return sendMessageAsync(buildQueueMessage(message));
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
        message.putUserProperty(SysProperties.MESSAGE_TYPE_KEY, queueMessage.getType());
        return message;
    }
}
