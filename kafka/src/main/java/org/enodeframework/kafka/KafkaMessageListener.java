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
import org.enodeframework.queue.MessageHandler;
import org.enodeframework.queue.MessageHandlerHolder;
import org.enodeframework.queue.QueueMessage;
import org.springframework.kafka.listener.AcknowledgingConsumerAwareMessageListener;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.listener.ConsumerAwareMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.lang.Nullable;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * @author anruence@gmail.com
 */
public class KafkaMessageListener implements AcknowledgingMessageListener<String, String>, ConsumerAwareMessageListener<String, String>, AcknowledgingConsumerAwareMessageListener<String, String> {

    private final MessageHandlerHolder messageHandlerHolder;

    public KafkaMessageListener(MessageHandlerHolder messageHandlerHolder) {
        this.messageHandlerHolder = messageHandlerHolder;
    }

    @Override
    public void onMessage(@Nonnull ConsumerRecord<String, String> data) {
        onMessage(data, null, null);
    }

    @Override
    public void onMessage(@Nonnull ConsumerRecord<String, String> data, @Nullable Consumer<?, ?> consumer) {
        onMessage(data, null, null);
    }

    @Override
    public void onMessage(@Nonnull ConsumerRecord<String, String> data, @Nullable Acknowledgment acknowledgment) {
        onMessage(data, acknowledgment, null);
    }

    private QueueMessage covertToQueueMessage(ConsumerRecord<String, String> record) {
        String mType = Optional.ofNullable(record.headers().lastHeader(SysProperties.MESSAGE_TYPE_KEY)).map(x -> new String(x.value())).orElse("");
        String tag = Optional.ofNullable(record.headers().lastHeader(SysProperties.MESSAGE_TAG_KEY)).map(x -> new String(x.value())).orElse("");
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setBody(record.value().getBytes(StandardCharsets.UTF_8));
        queueMessage.setType(mType);
        queueMessage.setTag(tag);
        queueMessage.setTopic(record.topic());
        queueMessage.setRouteKey(record.key());
        queueMessage.setKey(record.key());
        return queueMessage;
    }

    @Override
    public void onMessage(ConsumerRecord<String, String> data, @Nullable Acknowledgment acknowledgment, @Nullable Consumer<?, ?> consumer) {
        QueueMessage queueMessage = this.covertToQueueMessage(data);
        MessageHandler messageHandler = messageHandlerHolder.chooseMessageHandler(queueMessage.getType());
        messageHandler.handle(queueMessage, context -> {
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        });
    }
}
