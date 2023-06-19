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

import org.enodeframework.kafka.KafkaMessageListener;
import org.enodeframework.kafka.KafkaSendMessageService;
import org.enodeframework.kafka.KafkaSendReplyService;
import org.enodeframework.queue.MessageHandler;
import org.enodeframework.queue.MessageTypeCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;

@ConditionalOnProperty(prefix = "spring.enode", name = "mq", havingValue = "kafka")
public class EnodeKafkaAutoConfiguration {

    @Bean(name = "kafkaDomainEventListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "event")
    public KafkaMessageListener publishableExceptionListener(@Qualifier(value = "defaultPublishableExceptionMessageHandler") MessageHandler defaultPublishableExceptionMessageHandler, @Qualifier(value = "defaultApplicationMessageHandler") MessageHandler defaultApplicationMessageHandler, @Qualifier(value = "defaultDomainEventMessageHandler") MessageHandler defaultDomainEventMessageHandler) {
        Map<String, MessageHandler> messageHandlerMap = new HashMap<>();
        messageHandlerMap.put(MessageTypeCode.DomainEventMessage.getValue(), defaultDomainEventMessageHandler);
        messageHandlerMap.put(MessageTypeCode.ApplicationMessage.getValue(), defaultApplicationMessageHandler);
        messageHandlerMap.put(MessageTypeCode.ExceptionMessage.getValue(), defaultPublishableExceptionMessageHandler);
        return new KafkaMessageListener(messageHandlerMap);
    }

    @Bean(name = "kafkaCommandListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "command")
    public KafkaMessageListener commandListener(@Qualifier(value = "defaultCommandMessageHandler") MessageHandler defaultCommandMessageHandler) {
        Map<String, MessageHandler> messageHandlerMap = new HashMap<>();
        messageHandlerMap.put(MessageTypeCode.CommandMessage.getValue(), defaultCommandMessageHandler);
        return new KafkaMessageListener(messageHandlerMap);
    }

    @Bean(name = "kafkaSendMessageService")
    public KafkaSendMessageService kafkaSendMessageService(@Qualifier(value = "enodeKafkaTemplate") KafkaTemplate<String, String> kafkaTemplate) {
        return new KafkaSendMessageService(kafkaTemplate);
    }

    @Bean(name = "kafkaSendReplyService")
    public KafkaSendReplyService kafkaSendReplyService() {
        return new KafkaSendReplyService();
    }

    @Bean(name = "kafkaReplyListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "reply")
    public KafkaMessageListener replyListener(@Qualifier(value = "defaultReplyMessageHandler") MessageHandler defaultReplyMessageHandler) {
        Map<String, MessageHandler> messageHandlerMap = new HashMap<>();
        messageHandlerMap.put(MessageTypeCode.ReplyMessage.getValue(), defaultReplyMessageHandler);
        return new KafkaMessageListener(messageHandlerMap);
    }
}
