package org.enodeframework.test.config;

import org.enodeframework.amqp.message.AmqpBatchMessageListener;
import org.enodeframework.amqp.message.AmqpChannelAwareMessageListener;
import org.enodeframework.amqp.message.AmqpMessageListener;
import org.enodeframework.commanding.CommandOptions;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(prefix = "spring.enode", name = "mq", havingValue = "amqp")
@Configuration
public class EnodeTestAmqpConfig {

    @Value("${spring.enode.mq.topic.command}")
    private String commandTopic;

    @Value("${spring.enode.mq.topic.event}")
    private String eventTopic;

    @Bean
    Queue commandQueue() {
        return new Queue(commandTopic, false);
    }

    @Bean
    Queue eventQueue() {
        return new Queue(eventTopic, false);
    }

    @Bean
    Queue replyQueue(CommandOptions options) {
        return new Queue(options.getReplyTopic(), false);
    }

    @Bean
    TopicExchange commandExchange() {
        return new TopicExchange(commandTopic);
    }

    @Bean
    TopicExchange eventExchange() {
        return new TopicExchange(eventTopic);
    }

    @Bean
    TopicExchange replyExchange(CommandOptions options) {
        return new TopicExchange(options.getReplyTopic());
    }

    @Bean
    Binding binding(@Qualifier("commandQueue") Queue commandQueue,
                    @Qualifier("eventQueue") Queue eventQueue,
                    @Qualifier("replyQueue") Queue replyQueue,
                    @Qualifier("commandExchange") TopicExchange commandExchange,
                    @Qualifier("eventExchange") TopicExchange eventExchange,
                    @Qualifier("replyExchange") TopicExchange replyExchange,
                    CommandOptions options
    ) {
        return BindingBuilder.bind(eventQueue).to(eventExchange).with("#.#");
    }

    @Bean
    Binding commandBinding(@Qualifier("commandQueue") Queue commandQueue,
                           @Qualifier("eventQueue") Queue eventQueue,
                           @Qualifier("replyQueue") Queue replyQueue,
                           @Qualifier("commandExchange") TopicExchange commandExchange,
                           @Qualifier("eventExchange") TopicExchange eventExchange,
                           @Qualifier("replyExchange") TopicExchange replyExchange,
                           CommandOptions options
    ) {
        return BindingBuilder.bind(commandQueue).to(commandExchange).with("#.#");
    }

    @Bean
    Binding replyBinding(@Qualifier("commandQueue") Queue commandQueue,
                         @Qualifier("eventQueue") Queue eventQueue,
                         @Qualifier("replyQueue") Queue replyQueue,
                         @Qualifier("commandExchange") TopicExchange commandExchange,
                         @Qualifier("eventExchange") TopicExchange eventExchange,
                         @Qualifier("replyExchange") TopicExchange replyExchange,
                         CommandOptions options
    ) {
        return BindingBuilder.bind(replyQueue).to(replyExchange).with("#.#");
    }

    @Bean
    public MessageListenerAdapter messageListenerAdapter(AmqpMessageListener messageListener) {
        return new MessageListenerAdapter(messageListener);
    }

    @Bean
    public SimpleMessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory, MessageListenerAdapter messageListenerAdapter, CommandOptions options) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(commandTopic, eventTopic, options.getReplyTopic());
        container.setMessageListener(messageListenerAdapter);
        container.setAcknowledgeMode(AcknowledgeMode.AUTO);
        container.setMissingQueuesFatal(false);
        return container;
    }

    @Bean(name = "enodeAmqpTemplate")
    public AmqpTemplate enodeAmqpTemplate(RabbitTemplate template) {
        return template;
    }
}
