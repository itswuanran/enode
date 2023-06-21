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

import com.google.common.collect.Maps;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.common.extensions.SysProperties;
import org.enodeframework.queue.QueueMessage;
import org.enodeframework.queue.SendMessageResult;
import org.enodeframework.queue.SendMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class KafkaSendMessageService implements SendMessageService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaSendMessageService.class);
    private final KafkaTemplate<String, String> producer;

    public KafkaSendMessageService(KafkaTemplate<String, String> producer) {
        this.producer = producer;
    }

    @Override
    public CompletableFuture<SendMessageResult> sendMessageAsync(QueueMessage queueMessage) {
        ProducerRecord<String, String> message = this.covertToProducerRecord(queueMessage);
        return producer.send(message).handle((result, throwable) -> {
            if (throwable != null) {
                logger.error("Async send message has exception, message: {}", queueMessage, throwable);
                throw new IORuntimeException(throwable);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Async send message success, sendResult: {}, message: {}", result, queueMessage);
            }
            Map<String, Object> items = Maps.newHashMap();
            items.put("result", result);
            return new SendMessageResult("", items);
        });
    }

    private ProducerRecord<String, String> covertToProducerRecord(QueueMessage queueMessage) {
        ProducerRecord<String, String> record = new ProducerRecord<>(queueMessage.getTopic(), queueMessage.getRouteKey(), queueMessage.bodyAsStr());
        Header mTypeHeader = new RecordHeader(SysProperties.MESSAGE_TYPE_KEY, queueMessage.getType().getBytes());
        Header tagHeader = new RecordHeader(SysProperties.MESSAGE_TAG_KEY, queueMessage.getTag().getBytes());
        record.headers().add(mTypeHeader).add(tagHeader);
        return record;
    }


}
