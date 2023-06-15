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

import org.apache.pulsar.client.api.Producer;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.queue.QueueMessage;
import org.enodeframework.queue.SendMessageResult;
import org.enodeframework.queue.SendMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class PulsarSendMessageService implements SendMessageService {

    private static final Logger logger = LoggerFactory.getLogger(PulsarSendMessageService.class);

    private final Map<Character, Producer<byte[]>> producerMap;

    public PulsarSendMessageService(Map<Character, Producer<byte[]>> producerMap) {
        this.producerMap = producerMap;
    }

    @Override
    public CompletableFuture<SendMessageResult> sendMessageAsync(QueueMessage queueMessage) {
        Producer<byte[]> producer = producerMap.get(queueMessage.getType());
        CompletableFuture<SendMessageResult> future = new CompletableFuture<>();
        if (producer == null) {
            String msg =
                String.format("No producer for topic: [%s], %s", queueMessage.getType(), queueMessage.getTopic());
            future.completeExceptionally(new IORuntimeException(msg));
            logger.error(msg);
            return future;
        }
        producer.newMessage()
            .key(queueMessage.getRouteKey())
            .value(queueMessage.getBodyAndType().getBytes())
            .orderingKey(queueMessage.getKey().getBytes())
            .sendAsync()
            .whenComplete((messageId, throwable) -> {
                if (throwable != null) {
                    logger.error("Async send message has exception, message: {}", queueMessage, throwable);
                    future.completeExceptionally(new IORuntimeException(throwable));
                    return;
                }
                if (logger.isDebugEnabled()) {
                    logger.debug(
                        "Async send message success, sendResult: {}, message: {}", messageId, queueMessage);
                }
                future.complete(new SendMessageResult(new String(messageId.toByteArray()), messageId));
            });
        return future;
    }
}
