package org.enodeframework.test.config;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.SubscriptionType;
import org.enodeframework.commanding.CommandOptions;
import org.enodeframework.pulsar.message.PulsarMessageListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@ConditionalOnProperty(prefix = "spring.enode", name = "mq", havingValue = "pulsar")
@Configuration
@Import(EnodeTestPulsarConfig.ProducerConfiguration.class)
public class EnodeTestPulsarConfig {

    @Value("${spring.enode.mq.topic.command}")
    private String commandTopic;

    @Value("${spring.enode.mq.topic.event}")
    private String eventTopic;

    @Bean
    public PulsarClient pulsarClient() throws PulsarClientException {
        return PulsarClient.builder()
            .serviceUrl("pulsar://127.0.0.1:6650")
            .build();
    }

    @Bean
    public Consumer<byte[]> commandConsumer(PulsarClient pulsarClient, PulsarMessageListener pulsarCommandListener) throws PulsarClientException {
        return pulsarClient.newConsumer().messageListener(pulsarCommandListener)
            .topic(commandTopic)
            .subscriptionType(SubscriptionType.Key_Shared)
            .subscriptionName(Constants.DEFAULT_CONSUMER_GROUP0)
            .subscribe();
    }

    @Bean
    public Consumer<byte[]> eventConsumer(
        PulsarClient pulsarClient,
        PulsarMessageListener pulsarDomainEventListener) throws PulsarClientException {
        return pulsarClient.newConsumer().messageListener(pulsarDomainEventListener)
            .topic(eventTopic)
            .subscriptionType(SubscriptionType.Key_Shared)
            .subscriptionName(Constants.DEFAULT_CONSUMER_GROUP1)
            .subscribe();
    }

    static class ProducerConfiguration {
        @Value("${spring.enode.mq.topic.command}")
        private String commandTopic;

        @Value("${spring.enode.mq.topic.event}")
        private String eventTopic;

        @Bean(name = "enodePulsarCommandProducer")
        public Producer<byte[]> enodePulsarCommandProducer(PulsarClient pulsarClient) throws PulsarClientException {
            return pulsarClient.newProducer().topic(commandTopic).producerName(Constants.DEFAULT_PRODUCER_GROUP).create();
        }

        @Bean(name = "enodePulsarDomainEventProducer")
        public Producer<byte[]> enodePulsarEventProducer(PulsarClient pulsarClient) throws PulsarClientException {
            return pulsarClient.newProducer().topic(eventTopic).producerName(Constants.DEFAULT_PRODUCER_GROUP).create();
        }

        @Bean(name = "enodePulsarReplyProducer")
        public Producer<byte[]> enodePulsarReplyProducer(PulsarClient pulsarClient, CommandOptions commandOptions) throws PulsarClientException {
            return pulsarClient.newProducer().topic(commandOptions.replyTo()).producerName(Constants.DEFAULT_PRODUCER_GROUP).create();
        }
    }
}
