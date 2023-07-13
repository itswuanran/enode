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
import org.enodeframework.amqp.message.AmqpChannelAwareMessageListener;
import org.enodeframework.amqp.message.AmqpProducerHolder;
import org.enodeframework.amqp.message.AmqpSendReplyService;
import org.enodeframework.commanding.CommandOptions;
import org.enodeframework.common.serializing.SerializeService;
import org.enodeframework.kafka.message.KafkaProducerHolder;
import org.enodeframework.kafka.message.KafkaSendReplyService;
import org.enodeframework.ons.message.OnsMessageListener;
import org.enodeframework.ons.message.OnsProducerHolder;
import org.enodeframework.ons.message.OnsSendReplyService;
import org.enodeframework.pulsar.message.PulsarProducerHolder;
import org.enodeframework.pulsar.message.PulsarSendReplyService;
import org.enodeframework.queue.MessageHandlerHolder;
import org.enodeframework.queue.command.CommandResultProcessor;
import org.enodeframework.redis.message.RedisReplyMessageListener;
import org.enodeframework.redis.message.RedisSendReplyService;
import org.enodeframework.rocketmq.message.RocketMQProducerHolder;
import org.enodeframework.rocketmq.message.RocketMQSendReplyService;
import org.enodeframework.vertx.message.TcpReplyMessageListener;
import org.enodeframework.vertx.message.TcpSendReplyService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

public class EnodeReplyAutoConfig {

    @ConditionalOnProperty(prefix = "spring.enode", name = "reply", havingValue = "redis")
    static class RedisReply {

        @Bean(name = "redisSendReplyService")
        public RedisSendReplyService redisSendReplyService(CommandOptions commandOptions, @Qualifier("enodeReactiveStringRedisTemplate") ReactiveStringRedisTemplate enodeReactiveStringRedisTemplate, SerializeService serializeService) {
            return new RedisSendReplyService(commandOptions, enodeReactiveStringRedisTemplate, serializeService);
        }

        @Bean(name = "redisReplyMessageListener")
        public RedisReplyMessageListener redisReplyMessageListener(MessageHandlerHolder messageHandlerHolder) {
            return new RedisReplyMessageListener(messageHandlerHolder);
        }
    }

    @ConditionalOnProperty(prefix = "spring.enode", name = "reply", havingValue = "tcp", matchIfMissing = false)
    static class TcpReply {
        @Bean(name = "tcpServerListener")
        public TcpReplyMessageListener tcpReplyMessageListener(CommandResultProcessor commandResultProcessor, CommandOptions commandOptions) throws Exception {
            NetServerOptions option = new NetServerOptions();
            option.setHost(commandOptions.getHost());
            option.setPort(commandOptions.getPort());
            return new TcpReplyMessageListener(commandResultProcessor, option);
        }

        @Bean(name = "tcpSendReplyService")
        public TcpSendReplyService tcpSendReplyService() {
            return new TcpSendReplyService(new VertxOptions());
        }
    }

    @ConditionalOnProperty(prefix = "spring.enode", name = "reply", havingValue = "kafka", matchIfMissing = false)
    static class KafkaReply {
        @Bean(name = "kafkaSendReplyService")
        public KafkaSendReplyService kafkaSendReplyService(KafkaProducerHolder producerHolder, CommandOptions commandOptions, SerializeService serializeService) {
            return new KafkaSendReplyService(producerHolder, commandOptions, serializeService);
        }
    }

    @ConditionalOnProperty(prefix = "spring.enode", name = "reply", havingValue = "rocketmq", matchIfMissing = false)
    static class RocketMQReply {
        @Bean(name = "rocketMQSendReplyService")
        public RocketMQSendReplyService rocketMQSendReplyService(RocketMQProducerHolder producerHolder, CommandOptions commandOptions, SerializeService serializeService) {
            return new RocketMQSendReplyService(producerHolder, commandOptions, serializeService);
        }
    }

    @ConditionalOnProperty(prefix = "spring.enode", name = "reply", havingValue = "ons", matchIfMissing = false)
    static class OnsReply {
        @Bean(name = "onsSendReplyService")
        public OnsSendReplyService onsSendReplyService(OnsProducerHolder producerHolder, CommandOptions commandOptions, SerializeService serializeService) {
            return new OnsSendReplyService(producerHolder, commandOptions, serializeService);
        }

        @Bean(name = "onsReplyMessageListener")
        @ConditionalOnExpression(value = "#{!'ons'.equals('${spring.enode.mq}')}")
        public OnsMessageListener onsReplyMessageListener(MessageHandlerHolder messageHandlerHolder) {
            return new OnsMessageListener(messageHandlerHolder);
        }
    }

    @ConditionalOnProperty(prefix = "spring.enode", name = "reply", havingValue = "pulsar", matchIfMissing = false)
    static class PulsarReply {
        @Bean(name = "pulsarSendReplyService")
        public PulsarSendReplyService pulsarSendReplyService(PulsarProducerHolder pulsarProducerHolder, SerializeService serializeService) {
            return new PulsarSendReplyService(pulsarProducerHolder, serializeService);
        }
    }

    @ConditionalOnProperty(prefix = "spring.enode", name = "reply", havingValue = "amqp", matchIfMissing = false)
    static class AmqpReply {
        @Bean(name = "amqpSendReplyService")
        public AmqpSendReplyService amqpSendReplyService(AmqpProducerHolder pulsarProducerHolder, CommandOptions commandOptions, SerializeService serializeService) {
            return new AmqpSendReplyService(pulsarProducerHolder, commandOptions, serializeService);
        }

        @Bean(name = "amqpReplyMessageListener")
        @ConditionalOnExpression(value = "#{!'amqp'.equals('${spring.enode.mq}')}")
        public AmqpChannelAwareMessageListener amqpReplyMessageListener(MessageHandlerHolder messageHandlerHolder) {
            return new AmqpChannelAwareMessageListener(messageHandlerHolder);
        }
    }
}
