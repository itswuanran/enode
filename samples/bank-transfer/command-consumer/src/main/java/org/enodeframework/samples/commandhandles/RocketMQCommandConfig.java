package org.enodeframework.samples.commandhandles;

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
@Import(RocketMQCommandConfig.ProductConfiguration.class)
public class RocketMQCommandConfig {

    @Value("${spring.enode.mq.topic.command}")
    private String commandTopic;

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQPushConsumer defaultMQPushConsumer(RocketMQMessageListener rocketMQCommandListener) throws MQClientException {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
        defaultMQPushConsumer.setConsumerGroup(QueueProperties.DEFAULT_CONSUMER_GROUP3);
        defaultMQPushConsumer.setNamesrvAddr(QueueProperties.NAMESRVADDR);
        defaultMQPushConsumer.subscribe(commandTopic, "*");
        defaultMQPushConsumer.setMessageListener(rocketMQCommandListener);
        defaultMQPushConsumer.setConsumeMessageBatchMaxSize(200);
        return defaultMQPushConsumer;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQPushConsumer replyConsumer(CommandOptions options, RocketMQMessageListener rocketMQReplyListener) throws MQClientException {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
        defaultMQPushConsumer.setConsumerGroup(QueueProperties.DEFAULT_CONSUMER_GROUP0);
        defaultMQPushConsumer.setNamesrvAddr(QueueProperties.NAMESRVADDR);
        // 只订阅发送到自己服务器的消息
        defaultMQPushConsumer.subscribe(options.getReplyTopic(), options.address());
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
