package org.enodeframework.spring;

import com.aliyun.openservices.ons.api.Producer;
import org.enodeframework.ons.message.OnsMessageListener;
import org.enodeframework.ons.message.OnsSendMessageService;
import org.enodeframework.queue.MessageHandler;
import org.enodeframework.queue.MessageTypeCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

@ConditionalOnProperty(prefix = "spring.enode", name = "mq", havingValue = "ons")
public class EnodeOnsAutoConfig {

    @Bean(name = "onsDomainEventListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "event")
    public OnsMessageListener onsDomainEventListener(@Qualifier(value = "defaultPublishableExceptionMessageHandler") MessageHandler defaultPublishableExceptionMessageHandler, @Qualifier(value = "defaultApplicationMessageHandler") MessageHandler defaultApplicationMessageHandler, @Qualifier(value = "defaultDomainEventMessageHandler") MessageHandler defaultDomainEventMessageHandler) {
        Map<Character, MessageHandler> messageHandlerMap = new HashMap<>();
        messageHandlerMap.put(MessageTypeCode.DomainEventMessage.getValue(), defaultDomainEventMessageHandler);
        messageHandlerMap.put(MessageTypeCode.ApplicationMessage.getValue(), defaultApplicationMessageHandler);
        messageHandlerMap.put(MessageTypeCode.ExceptionMessage.getValue(), defaultPublishableExceptionMessageHandler);
        return new OnsMessageListener(messageHandlerMap);
    }

    @Bean(name = "onsCommandListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "command")
    public OnsMessageListener commandListener(@Qualifier(value = "defaultCommandMessageHandler") MessageHandler defaultCommandMessageHandler) {
        Map<Character, MessageHandler> messageHandlerMap = new HashMap<>();
        messageHandlerMap.put(MessageTypeCode.CommandMessage.getValue(), defaultCommandMessageHandler);
        return new OnsMessageListener(messageHandlerMap);
    }

    @Bean(name = "onsSendMessageService")
    public OnsSendMessageService onsSendMessageService(@Qualifier(value = "enodeOnsProducer") Producer producer) {
        return new OnsSendMessageService(producer);
    }
}
