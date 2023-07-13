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

import org.apache.rocketmq.client.consumer.listener.*
import org.apache.rocketmq.common.message.MessageExt
import org.enodeframework.common.extensions.SysProperties
import org.enodeframework.queue.MessageHandlerHolder
import org.enodeframework.queue.QueueMessage
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.function.Consumer

/**
 * @author anruence@gmail.com
 */
class RocketMQMessageListener(private val messageHandlerHolder: MessageHandlerHolder) : MessageListenerConcurrently {
    override fun consumeMessage(
        msgs: List<MessageExt>,
        context: ConsumeConcurrentlyContext,
    ): ConsumeConcurrentlyStatus {
        val latch = CountDownLatch(msgs.size)
        msgs.forEach(Consumer { msg: MessageExt ->
            val queueMessage = covertToQueueMessage(msg)
            val messageHandler = messageHandlerHolder.chooseMessageHandler(queueMessage.type)
            messageHandler.handle(queueMessage) { latch.countDown() }
        })
        latch.await()
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS
    }
}

class RocketMQMessageOrderlyListener(private val messageHandlerHolder: MessageHandlerHolder) : MessageListenerOrderly {
    override fun consumeMessage(msgs: List<MessageExt>, context: ConsumeOrderlyContext): ConsumeOrderlyStatus {
        val latch = CountDownLatch(msgs.size)
        msgs.forEach(Consumer { msg: MessageExt ->
            val queueMessage = covertToQueueMessage(msg)
            val messageHandler = messageHandlerHolder.chooseMessageHandler(queueMessage.type)
            messageHandler.handle(queueMessage) { latch.countDown() }
        })
        latch.await()
        return ConsumeOrderlyStatus.SUCCESS
    }
}

private fun covertToQueueMessage(messageExt: MessageExt): QueueMessage {
    val queueMessage = QueueMessage()
    val mType = messageExt.getUserProperty(SysProperties.MESSAGE_TYPE_KEY)
    val tag = Optional.ofNullable(messageExt.tags).orElse("")
    val key = Optional.ofNullable(messageExt.keys).orElse("")
    queueMessage.body = messageExt.body
    queueMessage.type = mType
    queueMessage.topic = messageExt.topic
    queueMessage.tag = tag
    queueMessage.key = key
    return queueMessage
}
