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

import org.enodeframework.commanding.CommandOptions;
import org.enodeframework.common.serializing.SerializeService;
import org.enodeframework.messaging.ReplyMessage;
import org.enodeframework.queue.MessageTypeCode;
import org.enodeframework.queue.QueueMessage;
import org.enodeframework.queue.SendMessageResult;
import org.enodeframework.queue.SendReplyService;
import org.enodeframework.queue.reply.GenericReplyMessage;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class PulsarSendReplyService implements SendReplyService {
    private final PulsarProducerHolder pulsarProducerHolder;
    private final SerializeService serializeService;

    public PulsarSendReplyService(PulsarProducerHolder pulsarProducerHolder, SerializeService serializeService) {
        this.pulsarProducerHolder = pulsarProducerHolder;
        this.serializeService = serializeService;
    }

    @NotNull
    @Override
    public CompletableFuture<SendMessageResult> send(@NotNull ReplyMessage message) {
        return pulsarProducerHolder.sendAsync(buildQueueMessage(message));
    }

    private QueueMessage buildQueueMessage(ReplyMessage replyMessage) {
        String topic = pulsarProducerHolder.chooseProducer(MessageTypeCode.ReplyMessage.getValue()).getTopic();
        GenericReplyMessage message = replyMessage.asGenericReplyMessage();
        QueueMessage queueMessage = replyMessage.asPartQueueMessage();
        queueMessage.setTopic(topic);
        queueMessage.setTag(replyMessage.getAddress());
        queueMessage.setBody(serializeService.serializeBytes(message));
        queueMessage.setType(MessageTypeCode.ReplyMessage.getValue());
        return queueMessage;
    }
}
