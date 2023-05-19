package org.enodeframework.spring;

import com.google.common.collect.Maps;
import org.apache.pulsar.client.api.Producer;
import org.enode.pulsar.message.PulsarMessageListener;
import org.enode.pulsar.message.PulsarSendMessageService;
import org.enodeframework.queue.MessageHandler;
import org.enodeframework.queue.MessageTypeCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

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

    @Bean(name = "pulsarDomainEventListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "event")
    public PulsarMessageListener pulsarDomainEventListener(@Qualifier(value = "defaultPublishableExceptionMessageHandler") MessageHandler defaultPublishableExceptionMessageHandler, @Qualifier(value = "defaultApplicationMessageHandler") MessageHandler defaultApplicationMessageHandler, @Qualifier(value = "defaultDomainEventMessageHandler") MessageHandler defaultDomainEventMessageHandler) {
        Map<Character, MessageHandler> messageHandlerMap = new HashMap<>();
        messageHandlerMap.put(MessageTypeCode.DomainEventMessage.getValue(), defaultDomainEventMessageHandler);
        messageHandlerMap.put(MessageTypeCode.ApplicationMessage.getValue(), defaultApplicationMessageHandler);
        messageHandlerMap.put(MessageTypeCode.ExceptionMessage.getValue(), defaultPublishableExceptionMessageHandler);
        return new PulsarMessageListener(messageHandlerMap);
    }

    @Bean(name = "pulsarCommandListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "command")
    public PulsarMessageListener pulsarCommandListener(@Qualifier(value = "defaultCommandMessageHandler") MessageHandler defaultCommandMessageHandler) {
        Map<Character, MessageHandler> messageHandlerMap = new HashMap<>();
        messageHandlerMap.put(MessageTypeCode.CommandMessage.getValue(), defaultCommandMessageHandler);
        return new PulsarMessageListener(messageHandlerMap);
    }

    @Bean(name = "pulsarSendMessageService")
    public PulsarSendMessageService pulsarSendMessageService() {
        Map<Character, Producer<byte[]>> producers = Maps.newHashMap();
        producers.put(MessageTypeCode.CommandMessage.getValue(), enodePulsarCommandProducer);
        producers.put(MessageTypeCode.DomainEventMessage.getValue(), enodePulsarDomainEventProducer);
        producers.put(MessageTypeCode.ExceptionMessage.getValue(), enodePulsarPublishableExceptionProducer);
        producers.put(MessageTypeCode.ApplicationMessage.getValue(), enodePulsarApplicationMessageProducer);
        return new PulsarSendMessageService(producers);
    }
}
