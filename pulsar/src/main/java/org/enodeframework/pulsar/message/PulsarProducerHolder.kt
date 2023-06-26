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
package org.enodeframework.pulsar.message

import com.google.common.collect.Maps
import org.apache.pulsar.client.api.MessageId
import org.apache.pulsar.client.api.Producer
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.exception.ProducerNotFoundException
import org.enodeframework.common.extensions.SysProperties
import org.enodeframework.queue.QueueMessage
import org.enodeframework.queue.SendMessageResult
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture

class PulsarProducerHolder {
    private val logger = LoggerFactory.getLogger(PulsarSendMessageService::class.java)

    private val producerMap: MutableMap<String, Producer<ByteArray>> = Maps.newHashMap()
    fun chooseProducer(type: String): Producer<ByteArray> {
        return producerMap[type] ?: throw ProducerNotFoundException("No producer for type: $type")
    }

    fun put(key: String, value: Producer<ByteArray>) {
        producerMap[key] = value
    }

    fun sendAsync(queueMessage: QueueMessage): CompletableFuture<SendMessageResult> {
        return chooseProducer(queueMessage.type).newMessage().key(queueMessage.routeKey)
            .property(SysProperties.MESSAGE_TYPE_KEY, queueMessage.type)
            .property(SysProperties.MESSAGE_TAG_KEY, queueMessage.tag).properties(queueMessage.items)
            .value(queueMessage.body).orderingKey(queueMessage.key.toByteArray(StandardCharsets.UTF_8)).sendAsync()
            .exceptionally { throwable: Throwable? ->
                logger.error(
                    "Async send message has exception, message: {}", queueMessage, throwable
                )
                throw IORuntimeException(throwable)
            }.thenApply { messageId: MessageId ->
                if (logger.isDebugEnabled) {
                    logger.debug(
                        "Async send message success, sendResult: {}, message: {}", messageId, queueMessage
                    )
                }
                SendMessageResult(String(messageId.toByteArray(), StandardCharsets.UTF_8))
            }
    }
}
