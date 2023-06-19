package org.enodeframework.test.config;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.enodeframework.commanding.CommandOptions;
import org.enodeframework.rocketmq.message.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@ConditionalOnProperty(prefix = "spring.enode", name = "mq", havingValue = "rocketmq")
@Configuration
@Import(EnodeTestRocketMQConfig.ProducerConfiguration.class)
public class EnodeTestRocketMQConfig {

    @Value("${spring.enode.mq.topic.command}")
    private String commandTopic;

    @Value("${spring.enode.mq.topic.event}")
    private String eventTopic;

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQPushConsumer commandConsumer(RocketMQMessageListener rocketMQCommandListener) throws MQClientException {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
        defaultMQPushConsumer.setConsumerGroup(Constants.DEFAULT_CONSUMER_GROUP0);
        defaultMQPushConsumer.setNamesrvAddr(Constants.NAMESRVADDR);
        defaultMQPushConsumer.subscribe(commandTopic, "*");
        defaultMQPushConsumer.setMessageListener(rocketMQCommandListener);
        defaultMQPushConsumer.setConsumeMessageBatchMaxSize(500);
        return defaultMQPushConsumer;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQPushConsumer domainEventConsumer(RocketMQMessageListener rocketMQDomainEventListener) throws MQClientException {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
        defaultMQPushConsumer.setConsumerGroup(Constants.DEFAULT_CONSUMER_GROUP1);
        defaultMQPushConsumer.setNamesrvAddr(Constants.NAMESRVADDR);
        defaultMQPushConsumer.subscribe(eventTopic, "*");
        defaultMQPushConsumer.setMessageListener(rocketMQDomainEventListener);
        defaultMQPushConsumer.setConsumeMessageBatchMaxSize(500);
        return defaultMQPushConsumer;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQPushConsumer replyConsumer(RocketMQMessageListener rocketMQReplyListener, CommandOptions commandOptions) throws MQClientException {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
        defaultMQPushConsumer.setConsumerGroup(Constants.DEFAULT_CONSUMER_GROUP3);
        defaultMQPushConsumer.setNamesrvAddr(Constants.NAMESRVADDR);
        defaultMQPushConsumer.subscribe(commandOptions.getReplyTopic(), commandOptions.address());
        defaultMQPushConsumer.setMessageListener(rocketMQReplyListener);
        return defaultMQPushConsumer;
    }

    static class ProducerConfiguration {
        @Bean(name = "enodeMQProducer", initMethod = "start", destroyMethod = "shutdown")
        public DefaultMQProducer defaultMQProducer() {
            DefaultMQProducer producer = new DefaultMQProducer();
            producer.setNamesrvAddr(Constants.NAMESRVADDR);
            producer.setProducerGroup(Constants.DEFAULT_PRODUCER_GROUP0);
            return producer;
        }
    }
}
