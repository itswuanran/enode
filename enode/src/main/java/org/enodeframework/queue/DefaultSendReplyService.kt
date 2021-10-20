package org.enodeframework.queue

import io.vertx.core.AbstractVerticle
import io.vertx.core.VertxOptions
import io.vertx.core.json.JsonObject
import org.enodeframework.commanding.CommandResult
import org.enodeframework.commanding.CommandReturnType
import org.enodeframework.common.io.Task
import org.enodeframework.queue.domainevent.DomainEventHandledMessage
import java.util.concurrent.CompletableFuture

/**
 * @author anruence@gmail.com
 */
class DefaultSendReplyService : AbstractVerticle(), ISendReplyService {
    private var started = false
    private var stoped = false
    private lateinit var pointEventBus: PointToPointEventBus

    override fun start() {
        if (!started) {
            pointEventBus = PointToPointEventBus(vertx, VertxOptions())
            started = true
        }
    }

    override fun stop() {
        if (!stoped) {
            pointEventBus.close()
            stoped = true
        }
    }

    override fun sendCommandReply(
        commandResult: CommandResult,
        address: String
    ): CompletableFuture<Boolean> {
        val replyMessage = JsonObject()
        replyMessage.put("code", CommandReturnType.CommandExecuted.value)
        replyMessage.put("commandResult", commandResult)
        return sendReply(replyMessage, address)
    }

    override fun sendEventReply(
        eventHandledMessage: DomainEventHandledMessage,
        address: String
    ): CompletableFuture<Boolean> {
        val replyMessage = JsonObject()
        replyMessage.put("code", CommandReturnType.EventHandled.value)
        replyMessage.put("eventHandledMessage", eventHandledMessage)
        return sendReply(replyMessage, address)
    }

    private fun sendReply(
        replyMessage: JsonObject,
        address: String
    ): CompletableFuture<Boolean> {
        pointEventBus.send(address, replyMessage)
        return Task.completedTask
    }
}