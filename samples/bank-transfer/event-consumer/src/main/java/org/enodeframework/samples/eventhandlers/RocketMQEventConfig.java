package org.enodeframework.samples.eventhandlers;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.enodeframework.rocketmq.message.RocketMQMessageListener;
import org.enodeframework.samples.QueueProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnProperty(prefix = "spring.enode", name = "mq", havingValue = "rocketmq")
@Import(RocketMQEventConfig.ProductConfiguration.class)
public class RocketMQEventConfig {

    @Value("${spring.enode.mq.topic.event}")
    private String eventTopic;

    @Autowired
    @Qualifier("rocketMQDomainEventListener")
    private RocketMQMessageListener rocketMQDomainEventListener;

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQPushConsumer eventConsumer() throws MQClientException {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
        defaultMQPushConsumer.setConsumerGroup(QueueProperties.DEFAULT_CONSUMER_GROUP0);
        defaultMQPushConsumer.setNamesrvAddr(QueueProperties.NAMESRVADDR);
        defaultMQPushConsumer.subscribe(eventTopic, "*");
        defaultMQPushConsumer.setMessageListener(rocketMQDomainEventListener);
        return defaultMQPushConsumer;
    }

    static class ProductConfiguration {
        @Bean(name = "enodeMQProducer", initMethod = "start", destroyMethod = "shutdown")
        public DefaultMQProducer defaultMQProducer() {
            DefaultMQProducer producer = new DefaultMQProducer();
            producer.setNamesrvAddr(QueueProperties.NAMESRVADDR);
            producer.setProducerGroup(QueueProperties.DEFAULT_PRODUCER_GROUP0);
            return producer;
        }
    }
}
