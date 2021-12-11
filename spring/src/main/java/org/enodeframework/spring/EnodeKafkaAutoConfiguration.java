package org.enodeframework.spring;

import org.enodeframework.kafka.KafkaMessageListener;
import org.enodeframework.kafka.KafkaSendMessageService;
import org.enodeframework.queue.MessageHandler;
import org.enodeframework.queue.MessageTypeCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;

@ConditionalOnProperty(prefix = "spring.enode", name = "mq", havingValue = "kafka")
public class EnodeKafkaAutoConfiguration {

    @Bean(name = "kafkaDomainEventListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "event")
    public KafkaMessageListener publishableExceptionListener(
        @Qualifier(value = "defaultPublishableExceptionMessageHandler") MessageHandler defaultPublishableExceptionMessageHandler,
        @Qualifier(value = "defaultApplicationMessageHandler") MessageHandler defaultApplicationMessageHandler,
        @Qualifier(value = "defaultDomainEventMessageHandler") MessageHandler defaultDomainEventMessageHandler
    ) {
        Map<Character, MessageHandler> messageHandlerMap = new HashMap<>();
        messageHandlerMap.put(MessageTypeCode.DomainEventMessage.getValue(), defaultDomainEventMessageHandler);
        messageHandlerMap.put(MessageTypeCode.ApplicationMessage.getValue(), defaultApplicationMessageHandler);
        messageHandlerMap.put(MessageTypeCode.ExceptionMessage.getValue(), defaultPublishableExceptionMessageHandler);
        return new KafkaMessageListener(messageHandlerMap);
    }

    @Bean(name = "kafkaCommandListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "command")
    public KafkaMessageListener commandListener(@Qualifier(value = "defaultCommandMessageHandler") MessageHandler defaultCommandMessageHandler) {
        Map<Character, MessageHandler> messageHandlerMap = new HashMap<>();
        messageHandlerMap.put(MessageTypeCode.CommandMessage.getValue(), defaultCommandMessageHandler);
        return new KafkaMessageListener(messageHandlerMap);
    }

    @Bean(name = "kafkaSendMessageService")
    public KafkaSendMessageService kafkaSendMessageService(@Qualifier(value = "enodeKafkaTemplate") KafkaTemplate<String, String> kafkaTemplate) {
        return new KafkaSendMessageService(kafkaTemplate);
    }
}