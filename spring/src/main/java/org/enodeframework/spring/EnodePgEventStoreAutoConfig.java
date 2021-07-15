package org.enodeframework.spring;

import io.vertx.pgclient.PgPool;
import org.enodeframework.configurations.EventStoreConfiguration;
import org.enodeframework.common.serializing.ISerializeService;
import org.enodeframework.eventing.IEventSerializer;
import org.enodeframework.pg.PgEventStore;
import org.enodeframework.pg.PgPublishedVersionStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "pg")
public class EnodePgEventStoreAutoConfig {

    @Bean
    public PgEventStore pgEventStore(@Qualifier("enodePgPool") PgPool pgPool, IEventSerializer eventSerializer, ISerializeService serializeService) {
        PgEventStore eventStore = new PgEventStore(pgPool, EventStoreConfiguration.pg(), eventSerializer, serializeService);
        return eventStore;
    }

    @Bean
    public PgPublishedVersionStore pgPublishedVersionStore(@Qualifier("enodePgPool") PgPool pgPool) {
        PgPublishedVersionStore versionStore = new PgPublishedVersionStore(pgPool, EventStoreConfiguration.pg());
        return versionStore;
    }
}
