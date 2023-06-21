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

import com.google.common.collect.Maps;
import org.apache.pulsar.client.api.Producer;
import org.enodeframework.common.serializing.SerializeService;
import org.enodeframework.pulsar.message.PulsarMessageListener;
import org.enodeframework.pulsar.message.PulsarSendMessageService;
import org.enodeframework.queue.MessageHandlerHolder;
import org.enodeframework.queue.MessageTypeCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import javax.annotation.Resource;
import java.util.Map;

@ConditionalOnProperty(prefix = "spring.enode", name = "mq", havingValue = "pulsar")
public class EnodePulsarAutoConfig {

    @Resource(name = "enodePulsarDomainEventProducer")
    private Producer<byte[]> enodePulsarDomainEventProducer;

    @Resource(name = "enodePulsarCommandProducer")
    private Producer<byte[]> enodePulsarCommandProducer;

    @Resource(name = "enodePulsarApplicationMessageProducer")
    private Producer<byte[]> enodePulsarApplicationMessageProducer;

    @Resource(name = "enodePulsarPublishableExceptionProducer")
    private Producer<byte[]> enodePulsarPublishableExceptionProducer;

    @Resource(name = "enodePulsarReplyProducer")
    private Producer<byte[]> enodePulsarReplyProducer;

    @Bean(name = "pulsarDomainEventListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "event")
    public PulsarMessageListener pulsarDomainEventListener(MessageHandlerHolder messageHandlerHolder) {
        return new PulsarMessageListener(messageHandlerHolder);
    }

    @Bean(name = "pulsarCommandListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "command")
    public PulsarMessageListener pulsarCommandListener(MessageHandlerHolder messageHandlerHolder) {
        return new PulsarMessageListener(messageHandlerHolder);
    }

    @Bean(name = "pulsarSendMessageService")
    public PulsarSendMessageService pulsarSendMessageService(SerializeService serializeService) {
        Map<String, Producer<byte[]>> producers = Maps.newHashMap();
        producers.put(MessageTypeCode.CommandMessage.getValue(), enodePulsarCommandProducer);
        producers.put(MessageTypeCode.DomainEventMessage.getValue(), enodePulsarDomainEventProducer);
        producers.put(MessageTypeCode.ExceptionMessage.getValue(), enodePulsarPublishableExceptionProducer);
        producers.put(MessageTypeCode.ApplicationMessage.getValue(), enodePulsarApplicationMessageProducer);
        producers.put(MessageTypeCode.ReplyMessage.getValue(), enodePulsarReplyProducer);
        return new PulsarSendMessageService(producers, serializeService);
    }
}
