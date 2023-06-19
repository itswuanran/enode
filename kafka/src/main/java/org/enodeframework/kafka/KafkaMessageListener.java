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

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.enodeframework.common.extensions.SysProperties;
import org.enodeframework.queue.MessageHandler;
import org.enodeframework.queue.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;

import java.util.Map;
import java.util.Optional;

/**
 * @author anruence@gmail.com
 */
public class KafkaMessageListener implements AcknowledgingMessageListener<String, String> {

    private static final Logger logger = LoggerFactory.getLogger(KafkaMessageListener.class);

    private final Map<String, MessageHandler> messageHandlerMap;

    public KafkaMessageListener(Map<String, MessageHandler> messageHandlerMap) {
        this.messageHandlerMap = messageHandlerMap;
    }

    /**
     * Invoked with data from kafka.
     *
     * @param data           the data to be processed.
     * @param acknowledgment the acknowledgment.
     */
    @Override
    public void onMessage(ConsumerRecord<String, String> data, Acknowledgment acknowledgment) {
        QueueMessage queueMessage = this.covertToQueueMessage(data);
        MessageHandler messageHandler = messageHandlerMap.get(queueMessage.getType());
        if (messageHandler == null) {
            logger.error("No messageHandler for message: {}.", queueMessage);
            return;
        }
        messageHandler.handle(queueMessage, context -> {
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        });
    }

    private QueueMessage covertToQueueMessage(ConsumerRecord<String, String> record) {
        String mType = Optional.ofNullable(record.headers().lastHeader(SysProperties.MESSAGE_TYPE_KEY)).map(x -> new String(x.value())).orElse("");
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setBody(record.value());
        queueMessage.setType(mType);
        queueMessage.setTopic(record.topic());
        queueMessage.setRouteKey(record.key());
        queueMessage.setKey(record.key());
        return queueMessage;
    }
}
