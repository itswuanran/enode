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

import org.enodeframework.amqp.message.AmqpMessageListener;
import org.enodeframework.amqp.message.AmqpProducerHolder;
import org.enodeframework.amqp.message.AmqpSendMessageService;
import org.enodeframework.queue.MessageHandlerHolder;
import org.springframework.amqp.core.AsyncAmqpTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@ConditionalOnProperty(prefix = "spring.enode", name = "mq", havingValue = "amqp")
public class EnodeAmqpAutoConfiguration {
    @Bean(name = "enodeAmqpMessageListener")
    public AmqpMessageListener enodeAmqpMessageListener(MessageHandlerHolder messageHandlerHolder) {
        return new AmqpMessageListener(messageHandlerHolder);
    }

    @Bean(name = "amqpSendMessageService")
    public AmqpSendMessageService amqpSendMessageService(@Qualifier(value = "amqpProducerHolder") AmqpProducerHolder amqpProducerHolder) {
        return new AmqpSendMessageService(amqpProducerHolder);
    }

    @Bean(name = "amqpProducerHolder")
    public AmqpProducerHolder amqpProducerHolder(@Qualifier(value = "enodeAsyncAmqpTemplate") AsyncAmqpTemplate asyncAmqpTemplate) {
        return new AmqpProducerHolder(asyncAmqpTemplate);
    }
}
