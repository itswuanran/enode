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

import com.rabbitmq.client.Channel
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.extensions.SysProperties
import org.enodeframework.queue.MessageHandlerHolder
import org.enodeframework.queue.QueueMessage
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.BatchMessageListener
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageListener
import org.springframework.amqp.rabbit.listener.api.ChannelAwareBatchMessageListener
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener
import org.springframework.lang.Nullable
import java.io.IOException
import java.util.concurrent.CountDownLatch

/**
 * @author anruence@gmail.com
 */
class AmqpMessageListener(private val messageHandlerHolder: MessageHandlerHolder) : MessageListener {
    override fun onMessage(message: Message) {
        onMessage(messageHandlerHolder, message, null)
    }
}

class AmqpChannelAwareMessageListener(private val messageHandlerHolder: MessageHandlerHolder) :
    ChannelAwareMessageListener {
    override fun onMessage(message: Message, @Nullable channel: Channel?) {
        onMessage(messageHandlerHolder, message, channel)
    }
}

class AmqpBatchMessageListener(private val messageHandlerHolder: MessageHandlerHolder) : BatchMessageListener {
    override fun onMessageBatch(messages: MutableList<Message>) {
        onMessageBatch(messageHandlerHolder, messages, null)
    }
}

class AmqpChannelAwareBatchMessageListener(private val messageHandlerHolder: MessageHandlerHolder) :
    ChannelAwareBatchMessageListener {

    override fun onMessageBatch(messages: List<Message>, channel: Channel) {
        onMessageBatch(messageHandlerHolder, messages, channel)
    }
}

private val logger = LoggerFactory.getLogger(AmqpMessageListener::class.java)
private fun covertToQueueMessage(record: Message): QueueMessage {
    val props = record.messageProperties
    val queueMessage = QueueMessage()
    queueMessage.body = record.body
    queueMessage.tag = props.consumerTag
    queueMessage.key = props.messageId
    queueMessage.topic = props.consumerQueue
    queueMessage.type = props.getHeader(SysProperties.MESSAGE_TYPE_KEY)
    return queueMessage
}

fun onMessage(messageHandlerHolder: MessageHandlerHolder, message: Message, @Nullable channel: Channel?) {
    val queueMessage = covertToQueueMessage(message)
    messageHandlerHolder.chooseMessageHandler(queueMessage.type).handle(queueMessage) {
        try {
            channel?.basicAck(message.messageProperties.deliveryTag, false)
        } catch (e: IOException) {
            logger.error("Acknowledge message failed: {}.", message, e)
            throw IORuntimeException(e)
        } finally {
        }
    }
}

fun onMessageBatch(messageHandlerHolder: MessageHandlerHolder, messages: List<Message>, @Nullable channel: Channel?) {
    val latch = CountDownLatch(messages.size)
    messages.forEach { message: Message ->
        val queueMessage = covertToQueueMessage(message)
        messageHandlerHolder.chooseMessageHandler(queueMessage.type).handle(queueMessage) {
            try {
                channel?.basicAck(message.messageProperties.deliveryTag, false)
            } catch (e: IOException) {
                logger.error("Acknowledge Batch message failed: {}.", message, e)
                throw IORuntimeException(e)
            } finally {
                latch.countDown()
            }
        }
    }
    latch.await()
}