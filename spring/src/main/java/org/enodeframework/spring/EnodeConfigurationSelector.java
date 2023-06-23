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

import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotationMetadata;

/**
 * A {@link DeferredImportSelector} implementation with the lowest order to import a
 * {@link EnodeBootstrapRegistrar} as late as possible.
 * {@link EnodeAutoConfiguration} as late as possible.
 *
 * @author anruence@gmail.com
 * @since 1.0.5
 */
@Order
public class EnodeConfigurationSelector implements DeferredImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{
            EnodeBootstrapRegistrar.class.getName(),
            EnodeBeanContainerAutoConfig.class.getName(),
            EnodeAutoConfiguration.class.getName(),
            EnodeMemoryEventStoreAutoConfig.class.getName(),
            EnodeJDBCMySQLEventStoreAutoConfig.class.getName(),
            EnodeJDBCPgEventStoreAutoConfig.class.getName(),
            EnodePgEventStoreAutoConfig.class.getName(),
            EnodeMySQLEventStoreAutoConfig.class.getName(),
            EnodeMongoEventStoreAutoConfig.class.getName(),
            EnodeKafkaAutoConfiguration.class.getName(),
            EnodeOnsAutoConfig.class.getName(),
            EnodePulsarAutoConfig.class.getName(),
            EnodeReplyAutoConfig.RedisReply.class.getName(),
            EnodeReplyAutoConfig.TcpReply.class.getName(),
            EnodeReplyAutoConfig.KafkaReply.class.getName(),
            EnodeReplyAutoConfig.PulsarReply.class.getName(),
            EnodeReplyAutoConfig.RocketMQReply.class.getName(),
            EnodeReplyAutoConfig.OnsReply.class.getName(),
            EnodeReplyAutoConfig.AmqpReply.class.getName(),
            EnodeRocketMQAutoConfig.class.getName(),
        };
    }
}
