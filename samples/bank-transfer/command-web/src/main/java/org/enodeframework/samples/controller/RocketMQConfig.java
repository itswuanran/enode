package org.enodeframework.samples.controller;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.enodeframework.samples.QueueProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "spring.enode", name = "mq", havingValue = "rocketmq")
public class RocketMQConfig {
    @Bean(name = "enodeMQProducer", initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQProducer commandProducer() {
        DefaultMQProducer producer = new DefaultMQProducer();
        producer.setNamesrvAddr(QueueProperties.NAMESRVADDR);
        producer.setProducerGroup(QueueProperties.DEFAULT_PRODUCER_GROUP0);
        return producer;
    }
}
