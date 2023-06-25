package org.enodeframework.samples.controller;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.enodeframework.commanding.CommandOptions;
import org.enodeframework.kafka.KafkaMessageListener;
import org.enodeframework.samples.QueueProperties;
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

import static org.enodeframework.samples.QueueProperties.DEFAULT_CONSUMER_GROUP0;
import static org.enodeframework.samples.QueueProperties.KAFKA_SERVER;

@Configuration
@ConditionalOnProperty(prefix = "spring.enode", name = "mq", havingValue = "kafka")
public class KafkaConfig {
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, QueueProperties.KAFKA_SERVER);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean(name = "enodeKafkaTemplate")
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }


    @Bean
    public ConcurrentMessageListenerContainer<String, String> replyListenerContainer(CommandOptions commandOptions, KafkaMessageListener kafkaReplyListener) {
        ContainerProperties properties = new ContainerProperties(commandOptions.replyTo());
        properties.setGroupId(DEFAULT_CONSUMER_GROUP0);
        properties.setMessageListener(kafkaReplyListener);
        properties.setMissingTopicsFatal(false);
        properties.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return new ConcurrentMessageListenerContainer<>(consumerFactory(), properties);
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVER);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, DEFAULT_CONSUMER_GROUP0);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }
}
