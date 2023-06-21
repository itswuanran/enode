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
package org.enodeframework.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.enodeframework.common.extensions.SysProperties;
import org.enodeframework.common.io.Task;
import org.enodeframework.queue.MessageHandler;
import org.enodeframework.queue.MessageHandlerHolder;
import org.enodeframework.queue.QueueMessage;
import org.springframework.kafka.listener.BatchAcknowledgingConsumerAwareMessageListener;
import org.springframework.kafka.listener.BatchAcknowledgingMessageListener;
import org.springframework.kafka.listener.BatchConsumerAwareMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

/**
 * @author anruence@gmail.com
 */
public class KafkaBatchMessageListener implements BatchAcknowledgingMessageListener<String, String>, BatchConsumerAwareMessageListener<String, String>, BatchAcknowledgingConsumerAwareMessageListener<String, String> {

    private final MessageHandlerHolder messageHandlerHolder;

    public KafkaBatchMessageListener(MessageHandlerHolder messageHandlerHolder) {
        this.messageHandlerHolder = messageHandlerHolder;
    }

    private QueueMessage covertToQueueMessage(ConsumerRecord<String, String> record) {
        String mType = Optional.ofNullable(record.headers().lastHeader(SysProperties.MESSAGE_TYPE_KEY)).map(x -> new String(x.value())).orElse("");
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setBody(record.value().getBytes(StandardCharsets.UTF_8));
        queueMessage.setType(mType);
        queueMessage.setTopic(record.topic());
        queueMessage.setRouteKey(record.key());
        queueMessage.setKey(record.key());
        return queueMessage;
    }

    @Override
    public void onMessage(@NonNull List<ConsumerRecord<String, String>> data) {
        onMessage(data, null, null);
    }

    @Override
    public void onMessage(@NonNull List<ConsumerRecord<String, String>> data, @Nullable Acknowledgment acknowledgment) {
        onMessage(data, acknowledgment, null);
    }

    @Override
    public void onMessage(@NonNull List<ConsumerRecord<String, String>> data, @Nullable Consumer<?, ?> consumer) {
        onMessage(data, null, consumer);
    }

    @Override
    public void onMessage(@NonNull List<ConsumerRecord<String, String>> data, @Nullable Acknowledgment acknowledgment, @Nullable Consumer<?, ?> consumer) {
        CountDownLatch latch = new CountDownLatch(data.size());
        data.forEach(message -> {
            QueueMessage queueMessage = this.covertToQueueMessage(message);
            MessageHandler messageHandler = messageHandlerHolder.chooseMessageHandler(queueMessage.getType());
            messageHandler.handle(queueMessage, context -> {
                latch.countDown();
            });
        });
        Task.await(latch);
        if (acknowledgment != null) {
            acknowledgment.acknowledge();
        }
    }
}
