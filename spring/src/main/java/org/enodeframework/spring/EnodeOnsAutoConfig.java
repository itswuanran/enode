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
import org.enodeframework.ons.message.OnsBatchMessageListener;
import org.enodeframework.ons.message.OnsMessageListener;
import org.enodeframework.ons.message.OnsMessageOrderListener;
import org.enodeframework.ons.message.OnsProducerHolder;
import org.enodeframework.ons.message.OnsSendMessageService;
import org.enodeframework.queue.MessageHandlerHolder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;

@ConditionalOnExpression(value = "#{'ons'.equals('${spring.enode.mq}') or 'ons'.equals('${spring.enode.reply}')}")
public class EnodeOnsAutoConfig {
    @Bean(name = "enodeOnsSendMessageService")
    public OnsSendMessageService enodeOnsSendMessageService(OnsProducerHolder onsProducerHolder) {
        return new OnsSendMessageService(onsProducerHolder);
    }

    @Bean(name = "enodeOnsProducerHolder")
    public OnsProducerHolder enodeOnsProducerHolder(@Qualifier(value = "enodeOnsProducer") Producer producer) {
        return new OnsProducerHolder(producer);
    }

    @Bean(name = "enodeOnsMessageListener")
    public OnsMessageListener enodeOnsMessageListener(MessageHandlerHolder messageHandlerHolder) {
        return new OnsMessageListener(messageHandlerHolder);
    }

    @Bean(name = "enodeOnsMessageOrderListener")
    public OnsMessageOrderListener enodeOnsMessageOrderListener(MessageHandlerHolder messageHandlerHolder) {
        return new OnsMessageOrderListener(messageHandlerHolder);
    }

    @Bean(name = "enodeOnsBatchMessageListener")
    public OnsBatchMessageListener enodeOnsBatchMessageListener(MessageHandlerHolder messageHandlerHolder) {
        return new OnsBatchMessageListener(messageHandlerHolder);
    }
}
