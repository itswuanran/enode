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

import io.vertx.pgclient.PgPool;
import org.enodeframework.common.serializing.SerializeService;
import org.enodeframework.eventing.EventSerializer;
import org.enodeframework.pg.PgEventStore;
import org.enodeframework.pg.PgPublishedVersionStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "pg")
public class EnodePgEventStoreAutoConfig {

    @Bean
    public PgEventStore pgEventStore(
        @Qualifier("enodePgPool") PgPool pgPool,
        EventSerializer eventSerializer,
        SerializeService serializeService) {
        PgEventStore eventStore =
            new PgEventStore(pgPool, DefaultEventStoreOptions.Driver.pg(), eventSerializer, serializeService);
        return eventStore;
    }

    @Bean
    public PgPublishedVersionStore pgPublishedVersionStore(@Qualifier("enodePgPool") PgPool pgPool) {
        PgPublishedVersionStore versionStore =
            new PgPublishedVersionStore(pgPool, DefaultEventStoreOptions.Driver.pg());
        return versionStore;
    }
}
