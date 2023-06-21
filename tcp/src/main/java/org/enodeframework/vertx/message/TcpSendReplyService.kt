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
package org.enodeframework.vertx.message

import io.vertx.core.AbstractVerticle
import io.vertx.core.VertxOptions
import io.vertx.core.json.JsonObject
import org.enodeframework.messaging.ReplyMessage
import org.enodeframework.queue.SendMessageResult
import org.enodeframework.queue.SendReplyService
import java.util.concurrent.CompletableFuture

/**
 * @author anruence@gmail.com
 */
class TcpSendReplyService(
    private var options: VertxOptions
) : AbstractVerticle(), SendReplyService {
    private var started = false
    private var stoped = false
    private lateinit var pointEventBus: PointToPointEventBus

    override fun start() {
        if (!started) {
            pointEventBus = PointToPointEventBus(vertx, options)
            started = true
        }
    }

    override fun stop() {
        if (!stoped) {
            pointEventBus.close()
            stoped = true
        }
    }

    override fun send(message: ReplyMessage): CompletableFuture<SendMessageResult> {
        val replyMessage = JsonObject()
        replyMessage.put("data", message.asGenericReplyMessage())
        pointEventBus.send(message.address, replyMessage)
        return CompletableFuture.completedFuture(SendMessageResult(message.id))
    }
}