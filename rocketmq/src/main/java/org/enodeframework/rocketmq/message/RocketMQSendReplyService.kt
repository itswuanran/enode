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
package org.enodeframework.rocketmq.message

import org.enodeframework.commanding.CommandOptions
import org.enodeframework.common.serializing.SerializeService
import org.enodeframework.messaging.ReplyMessage
import org.enodeframework.queue.MessageTypeCode
import org.enodeframework.queue.QueueMessage
import org.enodeframework.queue.SendMessageResult
import org.enodeframework.queue.SendReplyService
import java.util.concurrent.CompletableFuture

/**
 * @author anruence@gmail.com
 */
class RocketMQSendReplyService(
    private val producerHolder: RocketMQProducerHolder,
    private val commandOptions: CommandOptions,
    private val serializeService: SerializeService
) : SendReplyService {
    override fun send(message: ReplyMessage): CompletableFuture<SendMessageResult> {
        return producerHolder.send(buildQueueMessage(message))
    }

    private fun buildQueueMessage(replyMessage: ReplyMessage): QueueMessage {
        val message = replyMessage.asGenericReplyMessage()
        val queueMessage = replyMessage.asPartQueueMessage()
        queueMessage.topic = commandOptions.replyTopic
        queueMessage.tag = replyMessage.address
        queueMessage.body = serializeService.serializeBytes(message)
        queueMessage.type = MessageTypeCode.ReplyMessage.value
        return queueMessage
    }
}
