package org.enodeframework.spring;

import org.apache.rocketmq.client.producer.MQProducer;
import org.enodeframework.queue.MessageHandler;
import org.enodeframework.queue.MessageTypeCode;
import org.enodeframework.rocketmq.message.RocketMQMessageListener;
import org.enodeframework.rocketmq.message.RocketMQSendMessageService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

@ConditionalOnProperty(prefix = "spring.enode", name = "mq", havingValue = "rocketmq")
public class EnodeRocketMQAutoConfig {

    @Bean(name = "rocketMQDomainEventListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "event")
    public RocketMQMessageListener rocketMQDomainEventListener(@Qualifier(value = "defaultPublishableExceptionMessageHandler") MessageHandler defaultPublishableExceptionMessageHandler, @Qualifier(value = "defaultApplicationMessageHandler") MessageHandler defaultApplicationMessageHandler, @Qualifier(value = "defaultDomainEventMessageHandler") MessageHandler defaultDomainEventMessageHandler) {
        Map<Character, MessageHandler> messageHandlerMap = new HashMap<>();
        messageHandlerMap.put(MessageTypeCode.DomainEventMessage.getValue(), defaultDomainEventMessageHandler);
        messageHandlerMap.put(MessageTypeCode.ApplicationMessage.getValue(), defaultApplicationMessageHandler);
        messageHandlerMap.put(MessageTypeCode.ExceptionMessage.getValue(), defaultPublishableExceptionMessageHandler);
        return new RocketMQMessageListener(messageHandlerMap);
    }

    @Bean(name = "rocketMQCommandListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "command")
    public RocketMQMessageListener rocketMQCommandListener(@Qualifier(value = "defaultCommandMessageHandler") MessageHandler defaultCommandMessageHandler) {
        Map<Character, MessageHandler> messageHandlerMap = new HashMap<>();
        messageHandlerMap.put(MessageTypeCode.CommandMessage.getValue(), defaultCommandMessageHandler);
        return new RocketMQMessageListener(messageHandlerMap);
    }

    @Bean(name = "rocketMQSendMessageService")
    public RocketMQSendMessageService rocketMQSendMessageService(@Qualifier(value = "enodeMQProducer") MQProducer mqProducer) {
        return new RocketMQSendMessageService(mqProducer);
    }
}
