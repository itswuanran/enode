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
import org.enodeframework.queue.MessageHandlerHolder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

@ConditionalOnProperty(prefix = "spring.enode", name = "mq", havingValue = "kafka")
public class EnodeKafkaAutoConfiguration {
    @Bean(name = "enodeKafkaMessageListener")
    public KafkaMessageListener enodekKafkaMessageListener(MessageHandlerHolder messageHandlerHolder) {
        return new KafkaMessageListener(messageHandlerHolder);
    }

    @Bean(name = "kafkaSendMessageService")
    public KafkaSendMessageService kafkaSendMessageService(@Qualifier(value = "enodeKafkaTemplate") KafkaTemplate<String, String> kafkaTemplate) {
        return new KafkaSendMessageService(kafkaTemplate);
    }
}
