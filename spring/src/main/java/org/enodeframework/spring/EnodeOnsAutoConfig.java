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

import com.aliyun.openservices.ons.api.Producer;
import org.enodeframework.common.serializing.SerializeService;
import org.enodeframework.ons.message.OnsMessageListener;
import org.enodeframework.ons.message.OnsSendMessageService;
import org.enodeframework.queue.MessageHandler;
import org.enodeframework.queue.MessageHandlerHolder;
import org.enodeframework.queue.MessageTypeCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@ConditionalOnProperty(prefix = "spring.enode", name = "mq", havingValue = "ons")
public class EnodeOnsAutoConfig {
    @Value("${spring.enode.mq.topic.reply:}")
    private String replyTopic;

    @Bean(name = "onsDomainEventListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "event")
    public OnsMessageListener onsDomainEventListener(
        @Qualifier(value = "defaultPublishableExceptionMessageHandler") MessageHandler defaultPublishableExceptionMessageHandler,
        @Qualifier(value = "defaultApplicationMessageHandler") MessageHandler defaultApplicationMessageHandler,
        @Qualifier(value = "defaultDomainEventMessageHandler") MessageHandler defaultDomainEventMessageHandler) {
        MessageHandlerHolder messageHandlerMap = new MessageHandlerHolder();
        messageHandlerMap.put(MessageTypeCode.DomainEventMessage.getValue(), defaultDomainEventMessageHandler);
        messageHandlerMap.put(MessageTypeCode.ApplicationMessage.getValue(), defaultApplicationMessageHandler);
        messageHandlerMap.put(MessageTypeCode.ExceptionMessage.getValue(), defaultPublishableExceptionMessageHandler);
        return new OnsMessageListener(messageHandlerMap);
    }

    @Bean(name = "onsCommandListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "command")
    public OnsMessageListener onsCommandListener(
        @Qualifier(value = "defaultCommandMessageHandler") MessageHandler defaultCommandMessageHandler) {
        MessageHandlerHolder messageHandlerMap = new MessageHandlerHolder();
        messageHandlerMap.put(MessageTypeCode.CommandMessage.getValue(), defaultCommandMessageHandler);
        return new OnsMessageListener(messageHandlerMap);
    }

    @Bean(name = "onsSendMessageService")
    public OnsSendMessageService onsSendMessageService(@Qualifier(value = "enodeOnsProducer") Producer producer, SerializeService serializeService) {
        return new OnsSendMessageService(replyTopic, producer, serializeService);
    }

    @Bean(name = "onsReplyListener")
    public OnsMessageListener onsReplyListener(
        @Qualifier(value = "defaultReplyMessageHandler") MessageHandler defaultReplyMessageHandler) {
        MessageHandlerHolder messageHandlerMap = new MessageHandlerHolder();
        messageHandlerMap.put(MessageTypeCode.ReplyMessage.getValue(), defaultReplyMessageHandler);
        return new OnsMessageListener(messageHandlerMap);
    }
}
