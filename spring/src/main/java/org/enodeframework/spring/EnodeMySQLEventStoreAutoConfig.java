package org.enodeframework.spring;

import io.vertx.mysqlclient.MySQLPool;
import org.enodeframework.common.serializing.ISerializeService;
import org.enodeframework.configurations.EventStoreConfiguration;
import org.enodeframework.eventing.IEventSerializer;
import org.enodeframework.mysql.MySQLEventStore;
import org.enodeframework.mysql.MySQLPublishedVersionStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "mysql")
public class EnodeMySQLEventStoreAutoConfig {

    @Bean
    public MySQLEventStore mysqlEventStore(@Qualifier("enodeMySQLPool") MySQLPool pool, IEventSerializer eventSerializer, ISerializeService serializeService) {
        MySQLEventStore eventStore = new MySQLEventStore(pool, EventStoreConfiguration.mysql(), eventSerializer, serializeService);
        return eventStore;
    }

    @Bean
    public MySQLPublishedVersionStore mysqlPublishedVersionStore(@Qualifier("enodeMySQLPool") MySQLPool pool) {
        MySQLPublishedVersionStore publishedVersionStore = new MySQLPublishedVersionStore(pool, EventStoreConfiguration.mysql());
        return publishedVersionStore;
    }
}
