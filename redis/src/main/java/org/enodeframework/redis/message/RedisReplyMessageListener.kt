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
package org.enodeframework.redis.message

import org.enodeframework.queue.MessageHandlerHolder
import org.enodeframework.queue.MessageTypeCode
import org.enodeframework.queue.QueueMessage
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import java.nio.charset.StandardCharsets

/**
 * @author anruence@gmail.com
 */
class RedisReplyMessageListener(private val messageHandlerHolder: MessageHandlerHolder) : MessageListener {
    private fun convertQueueMessage(message: Message): QueueMessage {
        val queueMessage = QueueMessage()
        val channel = String(message.channel, StandardCharsets.UTF_8)
        queueMessage.type = MessageTypeCode.ReplyMessage.value
        queueMessage.topic = channel
        queueMessage.body = message.body
        return queueMessage
    }

    override fun onMessage(message: Message, pattern: ByteArray?) {
        val queueMessage = convertQueueMessage(message)
        messageHandlerHolder.chooseMessageHandler(queueMessage.type).handle(queueMessage) { }
    }
}
