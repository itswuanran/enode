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

import org.enodeframework.commanding.CommandOptions
import org.enodeframework.common.serializing.SerializeService
import org.enodeframework.messaging.ReplyMessage
import org.enodeframework.queue.SendMessageResult
import org.enodeframework.queue.SendReplyService
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import java.util.concurrent.CompletableFuture

/**
 * @author anruence@gmail.com
 */
class RedisSendReplyService(
    private val commandOptions: CommandOptions,
    private val reactiveRedisTemplate: ReactiveStringRedisTemplate,
    private val serializeService: SerializeService
) : SendReplyService {
    private val logger = LoggerFactory.getLogger(RedisSendReplyService::class.java)
    override fun send(message: ReplyMessage): CompletableFuture<SendMessageResult> {
        val genericReplyMessage = message.asGenericReplyMessage()
        val destination = commandOptions.replyWith(message.address)
        return reactiveRedisTemplate.convertAndSend(destination, serializeService.serialize(genericReplyMessage))
            .toFuture()
            .thenApply { x: Long ->
                if (logger.isDebugEnabled) {
                    logger.debug("Async send message success, sendResult: {}, message: {}", x, genericReplyMessage)
                }
                SendMessageResult(x.toString())
            }
    }
}
