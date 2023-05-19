package org.enodeframework.spring;

import org.enodeframework.common.serializing.SerializeService;
import org.enodeframework.eventing.EventSerializer;
import org.enodeframework.jdbc.JDBCEventStore;
import org.enodeframework.jdbc.JDBCPublishedVersionStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "jdbc-mysql")
public class EnodeJDBCMySQLEventStoreAutoConfig {

    @Bean
    public JDBCEventStore jdbcEventStore(@Qualifier("enodeMySQLDataSource") DataSource enodeMySQLDataSource, EventSerializer eventSerializer, SerializeService serializeService) {
        JDBCEventStore eventStore = new JDBCEventStore(enodeMySQLDataSource, DefaultEventStoreConfiguration.Driver.mysql(), eventSerializer, serializeService);
        return eventStore;
    }

    @Bean
    public JDBCPublishedVersionStore jdbcPublishedVersionStore(@Qualifier("enodeMySQLDataSource") DataSource enodeMySQLDataSource) {
        JDBCPublishedVersionStore publishedVersionStore = new JDBCPublishedVersionStore(enodeMySQLDataSource, DefaultEventStoreConfiguration.Driver.mysql());
        return publishedVersionStore;
    }
}
