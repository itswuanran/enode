package org.enodeframework.test.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.enodeframework.commanding.CommandConfiguration;
import org.enodeframework.kafka.KafkaMessageListener;
import org.enodeframework.queue.command.CommandResultProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

@ConditionalOnProperty(prefix = "spring.enode", name = "mq", havingValue = "kafka")
@Configuration
public class EnodeTestKafkaConfig {

    @Value("${spring.enode.mq.topic.command}")
    private String commandTopic;

    @Value("${spring.enode.mq.topic.event}")
    private String eventTopic;

    @Value("${spring.enode.reply.topic}")
    private String replyTopic;

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.KAFKA_SERVER);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, Constants.DEFAULT_PRODUCER_GROUP);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, String> commandListenerContainer(KafkaMessageListener kafkaCommandListener, ConsumerFactory<String, String> consumerFactory) {
        ContainerProperties properties = new ContainerProperties(commandTopic);
        properties.setGroupId(Constants.DEFAULT_CONSUMER_GROUP1);
        properties.setMessageListener(kafkaCommandListener);
        properties.setMissingTopicsFatal(false);
        properties.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return new ConcurrentMessageListenerContainer<>(consumerFactory, properties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode", name = "reply", havingValue = "kafka")
    public ConcurrentMessageListenerContainer<String, String> replyListenerContainer(CommandConfiguration commandConfiguration, KafkaMessageListener kafkaReplyListener, ConsumerFactory<String, String> consumerFactory) {
        ContainerProperties properties = new ContainerProperties(commandConfiguration.replyTo());
        properties.setGroupId(Constants.DEFAULT_CONSUMER_GROUP2);
        properties.setMessageListener(kafkaReplyListener);
        properties.setMissingTopicsFatal(false);
        properties.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return new ConcurrentMessageListenerContainer<>(consumerFactory, properties);
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, String> domainEventListenerContainer(KafkaMessageListener kafkaDomainEventListener, ConsumerFactory<String, String> consumerFactory) {
        ContainerProperties properties = new ContainerProperties(eventTopic);
        properties.setGroupId(Constants.DEFAULT_CONSUMER_GROUP3);
        properties.setMessageListener(kafkaDomainEventListener);
        properties.setMissingTopicsFatal(false);
        properties.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return new ConcurrentMessageListenerContainer<>(consumerFactory, properties);
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.KAFKA_SERVER);
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 500);
        props.put(ProducerConfig.METADATA_MAX_AGE_CONFIG, 60000);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean(name = "enodeKafkaTemplate")
    public KafkaTemplate<String, String> enodeKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
