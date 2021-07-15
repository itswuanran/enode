package org.enodeframework.spring;

import io.vertx.mysqlclient.MySQLPool;
import org.enodeframework.configurations.EventStoreConfiguration;
import org.enodeframework.common.serializing.ISerializeService;
import org.enodeframework.eventing.IEventSerializer;
import org.enodeframework.mysql.MysqlEventStore;
import org.enodeframework.mysql.MysqlPublishedVersionStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "mysql")
public class EnodeMySQLEventStoreAutoConfig {

    @Bean
    public MysqlEventStore mysqlEventStore(@Qualifier("enodeMySQLPool") MySQLPool pool, IEventSerializer eventSerializer, ISerializeService serializeService) {
        MysqlEventStore eventStore = new MysqlEventStore(pool, EventStoreConfiguration.mysql(), eventSerializer, serializeService);
        return eventStore;
    }

    @Bean
    public MysqlPublishedVersionStore mysqlPublishedVersionStore(@Qualifier("enodeMySQLPool") MySQLPool pool) {
        MysqlPublishedVersionStore publishedVersionStore = new MysqlPublishedVersionStore(pool, EventStoreConfiguration.mysql());
        return publishedVersionStore;
    }
}
