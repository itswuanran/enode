package org.enodeframework.samples.eventhandlers;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.enodeframework.commanding.CommandOptions;
import org.enodeframework.rocketmq.message.RocketMQMessageListener;
import org.enodeframework.samples.QueueProperties;
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

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQPushConsumer eventConsumer(RocketMQMessageListener rocketMQDomainEventListener) throws MQClientException {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
        defaultMQPushConsumer.setConsumerGroup(QueueProperties.DEFAULT_CONSUMER_GROUP0);
        defaultMQPushConsumer.setNamesrvAddr(QueueProperties.NAMESRVADDR);
        defaultMQPushConsumer.subscribe(eventTopic, "*");
        defaultMQPushConsumer.setMessageListener(rocketMQDomainEventListener);
        return defaultMQPushConsumer;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQPushConsumer replyConsumer(CommandOptions commandOptions, RocketMQMessageListener rocketMQReplyListener) throws MQClientException {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
        defaultMQPushConsumer.setConsumerGroup(QueueProperties.DEFAULT_CONSUMER_GROUP0);
        defaultMQPushConsumer.setNamesrvAddr(QueueProperties.NAMESRVADDR);
        // 只订阅发送到自己服务器的消息
        defaultMQPushConsumer.subscribe(commandOptions.getReplyTopic(), commandOptions.address());
        defaultMQPushConsumer.setMessageListener(rocketMQReplyListener);
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
