package org.enodeframework.test.config;

import org.enodeframework.commanding.CommandConfiguration;
import org.enodeframework.redis.message.RedisReplyMessageListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@ConditionalOnProperty(prefix = "spring.enode", name = "reply", havingValue = "redis")
@Configuration
public class EnodeTestRedisConfig {
    @Bean
    public ReactiveStringRedisTemplate enodeReactiveStringRedisTemplate() {
        return new ReactiveStringRedisTemplate(reactiveRedisConnectionFactory());
    }

    @Bean
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        return new LettuceConnectionFactory("localhost", 6379);
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory("localhost", 6379);
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisReplyMessageListener redisReplyMessageListener, CommandConfiguration commandConfiguration) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory());
        container.addMessageListener(redisReplyMessageListener, ChannelTopic.of(commandConfiguration.replyTo()));
        return container;
    }
}