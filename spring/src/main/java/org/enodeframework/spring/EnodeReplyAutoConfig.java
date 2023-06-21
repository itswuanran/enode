/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.enodeframework.spring;

import io.vertx.core.VertxOptions;
import io.vertx.core.net.NetServerOptions;
import org.enodeframework.commanding.CommandConfiguration;
import org.enodeframework.common.serializing.SerializeService;
import org.enodeframework.kafka.KafkaMessageListener;
import org.enodeframework.kafka.KafkaSendMessageService;
import org.enodeframework.kafka.KafkaSendReplyService;
import org.enodeframework.pulsar.message.PulsarMessageListener;
import org.enodeframework.queue.MessageHandlerHolder;
import org.enodeframework.queue.command.CommandResultProcessor;
import org.enodeframework.redis.message.RedisReplyMessageListener;
import org.enodeframework.redis.message.RedisSendReplyService;
import org.enodeframework.vertx.message.TcpSendReplyService;
import org.enodeframework.vertx.message.TcpServerListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

public class EnodeReplyAutoConfig {


    @ConditionalOnProperty(prefix = "spring.enode", name = "reply", havingValue = "redis")
    static class RedisReply {
        @Value("${spring.enode.reply.topic:}")
        private String queueName;

        @Bean
        public RedisSendReplyService redisSendReplyService(
            @Qualifier("enodeReactiveStringRedisTemplate") ReactiveStringRedisTemplate enodeReactiveStringRedisTemplate,
            SerializeService serializeService) {
            return new RedisSendReplyService(queueName, enodeReactiveStringRedisTemplate, serializeService);
        }

        @Bean
        public RedisReplyMessageListener redisReplyMessageListener(MessageHandlerHolder messageHandlerHolder) {
            return new RedisReplyMessageListener(messageHandlerHolder);
        }
    }

    @ConditionalOnProperty(prefix = "spring.enode", name = "reply", havingValue = "tcp", matchIfMissing = false)
    static class TcpReply {
        @Bean(name = "tcpServerListener")
        public TcpServerListener tcpServerListener(CommandResultProcessor commandResultProcessor, CommandConfiguration commandConfiguration) throws Exception {
            NetServerOptions option = new NetServerOptions();
            option.setHost(commandConfiguration.getHost());
            option.setPort(commandConfiguration.getPort());
            return new TcpServerListener(commandResultProcessor, option);
        }

        @Bean(name = "tcpSendReplyService")
        public TcpSendReplyService tcpSendReplyService() throws Exception {
            return new TcpSendReplyService(new VertxOptions());
        }
    }

    @ConditionalOnProperty(prefix = "spring.enode", name = "reply", havingValue = "kafka", matchIfMissing = false)
    static class KafkaReply {
        @Value("${spring.enode.reply.topic:}")
        private String replyTopic;

        @Bean(name = "kafkaSendReplyService")
        public KafkaSendReplyService kafkaSendReplyService(KafkaSendMessageService kafkaSendMessageService, SerializeService serializeService) {
            return new KafkaSendReplyService(replyTopic, kafkaSendMessageService, serializeService);
        }

        @Bean(name = "kafkaReplyListener")
        public KafkaMessageListener kafkaReplyListener(MessageHandlerHolder messageHandlerHolder) {
            return new KafkaMessageListener(messageHandlerHolder);
        }
    }

    @ConditionalOnProperty(prefix = "spring.enode", name = "reply", havingValue = "pulsar", matchIfMissing = false)
    static class PulsarReply {
        @Bean(name = "pulsarReplyListener")
        public PulsarMessageListener pulsarReplyListener(MessageHandlerHolder messageHandlerHolder) {
            return new PulsarMessageListener(messageHandlerHolder);
        }
    }
}
