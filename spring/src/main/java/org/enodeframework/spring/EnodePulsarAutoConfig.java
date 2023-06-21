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

import org.apache.pulsar.client.api.Producer;
import org.enodeframework.pulsar.message.PulsarProducerHolder;
import org.enodeframework.pulsar.message.PulsarMessageListener;
import org.enodeframework.pulsar.message.PulsarSendMessageService;
import org.enodeframework.queue.MessageHandlerHolder;
import org.enodeframework.queue.MessageTypeCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import javax.annotation.Resource;

@ConditionalOnProperty(prefix = "spring.enode", name = "mq", havingValue = "pulsar")
public class EnodePulsarAutoConfig {

    @Resource(name = "enodePulsarDomainEventProducer")
    private Producer<byte[]> enodePulsarDomainEventProducer;

    @Resource(name = "enodePulsarCommandProducer")
    private Producer<byte[]> enodePulsarCommandProducer;

    @Resource(name = "enodePulsarReplyProducer")
    private Producer<byte[]> enodePulsarReplyProducer;

    @Bean(name = "enodePulsarMessageListener")
    public PulsarMessageListener enodePulsarMessageListener(MessageHandlerHolder messageHandlerHolder) {
        return new PulsarMessageListener(messageHandlerHolder);
    }
    @Bean(name = "enodePulsarProducerHolder")
    public PulsarProducerHolder enodePulsaProducerHolder() {
        PulsarProducerHolder pulsarProducerHolder = new PulsarProducerHolder();
        pulsarProducerHolder.put(MessageTypeCode.CommandMessage.getValue(), enodePulsarCommandProducer);
        pulsarProducerHolder.put(MessageTypeCode.DomainEventMessage.getValue(), enodePulsarDomainEventProducer);
        pulsarProducerHolder.put(MessageTypeCode.ExceptionMessage.getValue(), enodePulsarDomainEventProducer);
        pulsarProducerHolder.put(MessageTypeCode.ApplicationMessage.getValue(), enodePulsarDomainEventProducer);
        pulsarProducerHolder.put(MessageTypeCode.ReplyMessage.getValue(), enodePulsarReplyProducer);
        return pulsarProducerHolder;
    }

    @Bean(name = "pulsarSendMessageService")
    public PulsarSendMessageService pulsarSendMessageService(PulsarProducerHolder pulsarProducerHolder) {
        return new PulsarSendMessageService(pulsarProducerHolder);
    }
}
