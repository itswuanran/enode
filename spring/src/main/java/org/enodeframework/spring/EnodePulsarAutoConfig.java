package org.enodeframework.spring;

import org.apache.pulsar.client.api.Producer;
import org.enode.pulsar.message.PulsarMessageListener;
import org.enode.pulsar.message.PulsarSendMessageService;
import org.enodeframework.queue.IMessageHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@ConditionalOnProperty(prefix = "spring.enode", name = "mq", havingValue = "pulsar")
public class EnodePulsarAutoConfig {

    @Resource(name = "enodePulsarDomainEventProducer")
    private Producer<byte[]> enodePulsarDomainEventProducer;

    @Resource(name = "enodePulsarCommandProducer")
    private Producer<byte[]> enodePulsarCommandProducer;

    @Resource(name = "enodePulsarApplicationMessageProducer")
    private Producer<byte[]> enodePulsarApplicationMessageProducer;

    @Resource(name = "enodePulsarPublishableExceptionProducer")
    private Producer<byte[]> enodePulsarPublishableExceptionProducer;

    @Bean(name = "pulsarPublishableExceptionListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "exception")
    public PulsarMessageListener publishableExceptionListener(@Qualifier(value = "defaultPublishableExceptionMessageHandler") IMessageHandler publishableExceptionListener) {
        return new PulsarMessageListener(publishableExceptionListener);
    }

    @Bean(name = "pulsarApplicationMessageListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "application")
    public PulsarMessageListener applicationMessageListener(@Qualifier(value = "defaultApplicationMessageHandler") IMessageHandler applicationMessageListener) {
        return new PulsarMessageListener(applicationMessageListener);
    }

    @Bean(name = "pulsarDomainEventListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "event")
    public PulsarMessageListener domainEventListener(@Qualifier(value = "defaultDomainEventMessageHandler") IMessageHandler domainEventListener) {
        return new PulsarMessageListener(domainEventListener);
    }

    @Bean(name = "pulsarCommandListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "command")
    public PulsarMessageListener commandListener(@Qualifier(value = "defaultCommandMessageHandler") IMessageHandler commandListener) {
        return new PulsarMessageListener(commandListener);
    }

    @Bean(name = "pulsarSendMessageService")
    public PulsarSendMessageService pulsarSendMessageService() {
        List<Producer<byte[]>> producers = new ArrayList<>();
        producers.add(enodePulsarCommandProducer);
        producers.add(enodePulsarDomainEventProducer);
        producers.add(enodePulsarPublishableExceptionProducer);
        producers.add(enodePulsarApplicationMessageProducer);
        return new PulsarSendMessageService(producers);
    }
}
