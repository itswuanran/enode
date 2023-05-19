package org.enodeframework.spring;

import io.vertx.mysqlclient.MySQLPool;
import org.enodeframework.common.serializing.SerializeService;
import org.enodeframework.eventing.EventSerializer;
import org.enodeframework.mysql.MySQLEventStore;
import org.enodeframework.mysql.MySQLPublishedVersionStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "mysql")
public class EnodeMySQLEventStoreAutoConfig {

    @Bean
    public MySQLEventStore mysqlEventStore(@Qualifier("enodeMySQLPool") MySQLPool pool, EventSerializer eventSerializer, SerializeService serializeService) {
        MySQLEventStore eventStore = new MySQLEventStore(pool, DefaultEventStoreConfiguration.Driver.mysql(), eventSerializer, serializeService);
        return eventStore;
    }

    @Bean
    public MySQLPublishedVersionStore mysqlPublishedVersionStore(@Qualifier("enodeMySQLPool") MySQLPool pool) {
        MySQLPublishedVersionStore publishedVersionStore = new MySQLPublishedVersionStore(pool, DefaultEventStoreConfiguration.Driver.mysql());
        return publishedVersionStore;
    }
}
