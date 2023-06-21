package org.enodeframework.test.config;

import org.enodeframework.queue.command.CommandResultProcessor;
import org.enodeframework.redis.message.RedisReplyMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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


    @Autowired
    CommandResultProcessor commandResultProcessor;

    @Value("${spring.enode.reply.topic}")
    private String replyTopic;

    @Bean
    public ReactiveStringRedisTemplate enodeReactiveStringRedisTemplate() {
        ReactiveStringRedisTemplate redisTemplate = new ReactiveStringRedisTemplate(reactiveRedisConnectionFactory());
        return redisTemplate;
    }

    @Bean
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        return new LettuceConnectionFactory("localhost", 6379);
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory("localhost", 6379);
    }

    public ChannelTopic topic() {
        return new ChannelTopic(replyTopic + "#" + commandResultProcessor.ReplyAddress());
    }

    @Bean
    public RedisMessageListenerContainer redisContainer(RedisReplyMessageListener redisReplyMessageListener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory());
        container.addMessageListener(redisReplyMessageListener, topic());
        return container;
    }
}