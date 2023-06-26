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
package org.enodeframework.amqp.message

import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.extensions.SysProperties
import org.enodeframework.queue.QueueMessage
import org.enodeframework.queue.SendMessageResult
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties
import reactor.core.publisher.Mono
import java.util.concurrent.CompletableFuture

class AmqpProducerHolder(private val asyncAmqpTemplate: AmqpTemplate) {

    private val logger = LoggerFactory.getLogger(AmqpProducerHolder::class.java)

    fun send(queueMessage: QueueMessage): CompletableFuture<SendMessageResult> {
        return Mono.fromCallable {
            asyncAmqpTemplate.send(
                queueMessage.topic,
                "${queueMessage.type}.${queueMessage.tag}",
                covertToAmqpMessage(queueMessage)
            )
        }.toFuture().exceptionally { throwable: Throwable? ->
            logger.error(
                "Async send message failed, error: {}, message: {}",
                throwable,
                queueMessage
            )
            throw IORuntimeException(throwable)
        }.thenApply {
            if (logger.isDebugEnabled) {
                logger.debug(
                    "Async send message success, message: {}", queueMessage
                )
            }
            SendMessageResult("")
        }
    }

    private fun covertToAmqpMessage(queueMessage: QueueMessage): Message {
        val props = MessageProperties()
        props.messageId = queueMessage.key
        props.consumerQueue = queueMessage.topic
        props.consumerTag = queueMessage.tag
        props.setHeader(SysProperties.MESSAGE_TYPE_KEY, queueMessage.type)
        props.setHeader(SysProperties.MESSAGE_TAG_KEY, queueMessage.tag)
        return Message(queueMessage.body, props)
    }
}
