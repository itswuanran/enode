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
package org.enodeframework.spring;

import kotlinx.coroutines.Dispatchers;
import org.enodeframework.kafka.message.*;
import org.enodeframework.queue.MessageHandlerHolder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;


@ConditionalOnExpression(value = "#{'kafka'.equals('${spring.enode.mq}') or 'kafka'.equals('${spring.enode.reply}')}")
public class EnodeKafkaAutoConfiguration {

    @Bean(name = "enodeKafkaMessageListener")
    public KafkaMessageListener enodeKafkaMessageListener(MessageHandlerHolder messageHandlerHolder) {
        return new KafkaMessageListener(messageHandlerHolder);
    }

    @Bean(name = "enodeKafkaConsumerAwareMessageListener")
    public KafkaConsumerAwareMessageListener enodeKafkaConsumerAwareMessageListener(MessageHandlerHolder messageHandlerHolder) {
        return new KafkaConsumerAwareMessageListener(messageHandlerHolder);
    }

    @Bean(name = "enodeKafkaAcknowledgingMessageListener")
    public KafkaAcknowledgingMessageListener enodeKafkaAcknowledgingMessageListener(MessageHandlerHolder messageHandlerHolder) {
        return new KafkaAcknowledgingMessageListener(messageHandlerHolder);
    }

    @Bean(name = "enodeKafkaAcknowledgingConsumerAwareMessageListener")
    public KafkaAcknowledgingConsumerAwareMessageListener enodeKafkaAcknowledgingConsumerAwareMessageListener(MessageHandlerHolder messageHandlerHolder) {
        return new KafkaAcknowledgingConsumerAwareMessageListener(messageHandlerHolder);
    }

    @Bean(name = "enodeKafkaBatchMessageListener")
    public KafkaBatchMessageListener enodeKafkaBatchMessageListener(MessageHandlerHolder messageHandlerHolder) {
        return new KafkaBatchMessageListener(messageHandlerHolder);
    }

    @Bean(name = "enodeKafkaBatchAcknowledgingMessageListener")
    public KafkaBatchAcknowledgingMessageListener enodeKafkaBatchAcknowledgingMessageListener(MessageHandlerHolder messageHandlerHolder) {
        return new KafkaBatchAcknowledgingMessageListener(messageHandlerHolder);
    }

    @Bean(name = "enodeKafkaBatchConsumerAwareMessageListener")
    public KafkaBatchConsumerAwareMessageListener enodeKafkaBatchConsumerAwareMessageListener(MessageHandlerHolder messageHandlerHolder) {
        return new KafkaBatchConsumerAwareMessageListener(messageHandlerHolder);
    }

    @Bean(name = "enodeKafkaBatchAcknowledgingConsumerAwareMessageListener")
    public KafkaBatchAcknowledgingConsumerAwareMessageListener enodeKafkaBatchAcknowledgingConsumerAwareMessageListener(MessageHandlerHolder messageHandlerHolder) {
        return new KafkaBatchAcknowledgingConsumerAwareMessageListener(messageHandlerHolder);
    }

    @Bean(name = "kafkaSendMessageService")
    public KafkaSendMessageService kafkaSendMessageService(@Qualifier(value = "kafkaProducerHolder") KafkaProducerHolder kafkaProducerHolder) {
        return new KafkaSendMessageService(kafkaProducerHolder);
    }

    @Bean(name = "kafkaProducerHolder")
    public KafkaProducerHolder kafkaProducerHolder(@Qualifier(value = "enodeKafkaTemplate") KafkaTemplate<String, String> kafkaTemplate) {
        return new KafkaProducerHolder(kafkaTemplate, Dispatchers.getIO());
    }
}
