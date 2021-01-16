package org.enodeframework.spring;

import io.vertx.ext.sql.SQLClient;
import org.enodeframework.common.EventStoreConfiguration;
import org.enodeframework.common.serializing.ISerializeService;
import org.enodeframework.eventing.IEventSerializer;
import org.enodeframework.jdbc.JDBCEventStore;
import org.enodeframework.jdbc.JDBCPublishedVersionStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "jdbc")
public class EnodeJDBCEventStoreAutoConfig {

    @Bean
    public JDBCEventStore jdbcEventStore(@Qualifier("enodeSQLClient") SQLClient client, IEventSerializer eventSerializer, ISerializeService serializeService) {
        JDBCEventStore eventStore = new JDBCEventStore(client, EventStoreConfiguration.mysql(), eventSerializer, serializeService);
        return eventStore;
    }

    @Bean
    public JDBCPublishedVersionStore jdbcPublishedVersionStore(@Qualifier("enodeSQLClient") SQLClient client) {
        JDBCPublishedVersionStore publishedVersionStore = new JDBCPublishedVersionStore(client, EventStoreConfiguration.mysql());
        return publishedVersionStore;
    }
}
