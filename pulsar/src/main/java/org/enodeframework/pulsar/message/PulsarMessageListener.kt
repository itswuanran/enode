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

import org.apache.pulsar.client.api.Consumer
import org.apache.pulsar.client.api.Message
import org.apache.pulsar.client.api.MessageListener
import org.apache.pulsar.client.api.PulsarClientException
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.extensions.SysProperties
import org.enodeframework.queue.MessageHandlerHolder
import org.enodeframework.queue.QueueMessage
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets

/**
 * @author anruence@gmail.com
 */
class PulsarMessageListener(private val messageHandlerHolder: MessageHandlerHolder) : MessageListener<ByteArray> {

    private val logger = LoggerFactory.getLogger(PulsarMessageListener::class.java)

    override fun received(consumer: Consumer<ByteArray>, msg: Message<ByteArray>) {
        val queueMessage = toQueueMessage(msg)
        val messageHandler = messageHandlerHolder.chooseMessageHandler(queueMessage.type)
        messageHandler.handle(queueMessage) {
            try {
                consumer.acknowledge(msg)
            } catch (e: PulsarClientException) {
                logger.error("Acknowledge message failed: {}.", queueMessage, e)
                throw IORuntimeException(e)
            }
        }
    }

    private fun toQueueMessage(messageExt: Message<ByteArray>): QueueMessage {
        val queueMessage = QueueMessage()
        val mType = messageExt.getProperty(SysProperties.MESSAGE_TYPE_KEY)
        val tag = messageExt.getProperty(SysProperties.MESSAGE_TAG_KEY)
        queueMessage.body = messageExt.value
        queueMessage.type = mType
        queueMessage.tag = tag
        queueMessage.topic = messageExt.topicName
        queueMessage.routeKey = messageExt.key
        queueMessage.key = String(messageExt.orderingKey, StandardCharsets.UTF_8)
        return queueMessage
    }
}
