package org.enodeframework.test.config;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.SubscriptionType;
import org.enode.pulsar.message.PulsarMessageListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(prefix = "spring.enode", name = "mq", havingValue = "pulsar")
@Configuration
public class EnodeTestPulsarConfig {

    @Value("${spring.enode.mq.topic.command}")
    private String commandTopic;

    @Value("${spring.enode.mq.topic.event}")
    private String eventTopic;

    @Value("${spring.enode.mq.topic.application}")
    private String applicationTopic;

    @Value("${spring.enode.mq.topic.exception}")
    private String exceptionTopic;

    @Bean
    public PulsarClient pulsarClient() throws PulsarClientException {
        return PulsarClient.builder()
                .serviceUrl("pulsar://localhost:6650")
                .build();
    }

    @Bean
    public Consumer<byte[]> commandConsumer(PulsarClient pulsarClient, @Qualifier("pulsarCommandListener")
            PulsarMessageListener pulsarCommandListener) throws PulsarClientException {
        return pulsarClient.newConsumer().messageListener(pulsarCommandListener)
                .topic(commandTopic)
                .subscriptionType(SubscriptionType.Key_Shared)
                .subscriptionName(Constants.DEFAULT_CONSUMER_GROUP0)
                .subscribe();
    }

    @Bean
    public Consumer<byte[]> eventConsumer(PulsarClient pulsarClient, @Qualifier("pulsarDomainEventListener")
            PulsarMessageListener pulsarDomainEventListener) throws PulsarClientException {
        return pulsarClient.newConsumer().messageListener(pulsarDomainEventListener)
                .topic(eventTopic)
                .subscriptionType(SubscriptionType.Key_Shared)
                .subscriptionName(Constants.DEFAULT_CONSUMER_GROUP1)
                .subscribe();
    }

    @Bean
    public Consumer<byte[]> applicationConsumer(PulsarClient pulsarClient, @Qualifier("pulsarApplicationMessageListener")
            PulsarMessageListener pulsarApplicationMessageListener) throws PulsarClientException {
        return pulsarClient.newConsumer().messageListener(pulsarApplicationMessageListener)
                .topic(applicationTopic)
                .subscriptionType(SubscriptionType.Key_Shared)
                .subscriptionName(Constants.DEFAULT_CONSUMER_GROUP2)
                .subscribe();
    }

    @Bean
    public Consumer<byte[]> exceptionConsumer(PulsarClient pulsarClient, @Qualifier("pulsarPublishableExceptionListener")
            PulsarMessageListener pulsarPublishableExceptionListener) throws PulsarClientException {
        return pulsarClient.newConsumer().messageListener(pulsarPublishableExceptionListener)
                .topic(exceptionTopic)
                .subscriptionType(SubscriptionType.Key_Shared)
                .subscriptionName(Constants.DEFAULT_CONSUMER_GROUP3)
                .subscribe();
    }

    @Bean(name = "enodePulsarCommandProducer")
    public Producer<byte[]> enodePulsarCommandProducer(PulsarClient pulsarClient) throws PulsarClientException {
        return pulsarClient.newProducer().topic(commandTopic).producerName(Constants.DEFAULT_PRODUCER_GROUP).create();
    }

    @Bean(name = "enodePulsarDomainEventProducer")
    public Producer<byte[]> enodePulsarEventProducer(PulsarClient pulsarClient) throws PulsarClientException {
        return pulsarClient.newProducer().topic(eventTopic).producerName(Constants.DEFAULT_PRODUCER_GROUP).create();
    }

    @Bean(name = "enodePulsarApplicationMessageProducer")
    public Producer<byte[]> enodePulsarApplicationProducer(PulsarClient pulsarClient) throws PulsarClientException {
        return pulsarClient.newProducer().topic(applicationTopic).producerName(Constants.DEFAULT_PRODUCER_GROUP).create();
    }

    @Bean(name = "enodePulsarPublishableExceptionProducer")
    public Producer<byte[]> enodePulsarExceptionProducer(PulsarClient pulsarClient) throws PulsarClientException {
        return pulsarClient.newProducer().topic(exceptionTopic).producerName(Constants.DEFAULT_PRODUCER_GROUP).create();
    }
}
