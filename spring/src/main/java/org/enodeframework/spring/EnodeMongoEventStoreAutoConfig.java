package org.enodeframework.spring;

import io.vertx.ext.mongo.MongoClient;
import org.enodeframework.common.serializing.SerializeService;
import org.enodeframework.eventing.EventSerializer;
import org.enodeframework.mongo.MongoEventStore;
import org.enodeframework.mongo.MongoPublishedVersionStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "mongo")
public class EnodeMongoEventStoreAutoConfig {

    @Bean
    public MongoEventStore mongoEventStore(@Qualifier("enodeMongoClient") MongoClient mongoClient, EventSerializer eventSerializer, SerializeService serializeService) {
        return new MongoEventStore(mongoClient, DefaultEventStoreConfiguration.Driver.mongo(), eventSerializer, serializeService);
    }

    @Bean
    public MongoPublishedVersionStore mongoPublishedVersionStore(@Qualifier("enodeMongoClient") MongoClient mongoClient) {
        return new MongoPublishedVersionStore(mongoClient, DefaultEventStoreConfiguration.Driver.mongo());
    }
}
