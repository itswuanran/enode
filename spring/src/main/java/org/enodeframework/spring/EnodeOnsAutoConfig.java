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
import org.enodeframework.ons.message.OnsMessageListener;
import org.enodeframework.ons.message.OnsProducerHolder;
import org.enodeframework.ons.message.OnsSendMessageService;
import org.enodeframework.queue.MessageHandlerHolder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@ConditionalOnProperty(prefix = "spring.enode", name = "mq", havingValue = "ons")
public class EnodeOnsAutoConfig {
    @Bean(name = "onsSendMessageService")
    public OnsSendMessageService onsSendMessageService(OnsProducerHolder onsProducerHolder) {
        return new OnsSendMessageService(onsProducerHolder);
    }

    @Bean(name = "onsProducerHolder")
    public OnsProducerHolder onsProducerHolder(@Qualifier(value = "enodeOnsProducer") Producer producer) {
        return new OnsProducerHolder(producer);
    }

    @Bean(name = "onsMessageListener")
    public OnsMessageListener onsMessageListener(MessageHandlerHolder messageHandlerHolder) {
        return new OnsMessageListener(messageHandlerHolder);
    }
}
