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

import org.apache.rocketmq.client.producer.MQProducer;
import org.enodeframework.queue.MessageHandlerHolder;
import org.enodeframework.rocketmq.message.RocketMQMessageListener;
import org.enodeframework.rocketmq.message.RocketMQMessageOrderlyListener;
import org.enodeframework.rocketmq.message.RocketMQProducerHolder;
import org.enodeframework.rocketmq.message.RocketMQSendMessageService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;

@ConditionalOnExpression(value = "#{'rocketmq'.equals('${spring.enode.mq}') or 'rocketmq'.equals('${spring.enode.reply}')}")
public class EnodeRocketMQAutoConfig {

    @Bean(name = "rocketMQMessageOrderlyListener")
    public RocketMQMessageOrderlyListener rocketMQMessageOrderlyListener(MessageHandlerHolder messageHandlerHolder) {
        return new RocketMQMessageOrderlyListener(messageHandlerHolder);
    }

    @Bean(name = "rocketMQMessageListener")
    public RocketMQMessageListener rocketMQMessageListener(MessageHandlerHolder messageHandlerHolder) {
        return new RocketMQMessageListener(messageHandlerHolder);
    }

    @Bean(name = "rocketMQSendMessageService")
    public RocketMQSendMessageService rocketMQSendMessageService(RocketMQProducerHolder rocketMQProducerHolder) {
        return new RocketMQSendMessageService(rocketMQProducerHolder);
    }

    @Bean(name = "rocketMQProducerHolder")
    public RocketMQProducerHolder rocketMQProducerHolder(@Qualifier(value = "enodeMQProducer") MQProducer mqProducer) {
        return new RocketMQProducerHolder(mqProducer);
    }
}
