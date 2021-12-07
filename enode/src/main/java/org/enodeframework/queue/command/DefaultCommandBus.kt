package org.enodeframework.queue.command

import kotlinx.coroutines.future.asDeferred
import org.enodeframework.commanding.CommandBus
import org.enodeframework.commanding.CommandMessage
import org.enodeframework.commanding.CommandResult
import org.enodeframework.commanding.CommandReturnType
import org.enodeframework.common.serializing.SerializeService
import org.enodeframework.common.utils.Assert
import org.enodeframework.common.utils.ReplyUtil
import org.enodeframework.queue.QueueMessage
import org.enodeframework.queue.SendMessageService
import java.util.concurrent.CompletableFuture

/**
 * @author anruence@gmail.com
 */
class DefaultCommandBus(
    private val topic: String,
    private val tag: String,
    private val commandResultProcessor: CommandResultProcessor,
    private val sendMessageService: SendMessageService,
    private val serializeService: SerializeService,
) : CommandBus {
    override fun sendAsync(command: CommandMessage<*>): CompletableFuture<Boolean> {
        return sendMessageService.sendMessageAsync(buildCommandMessage(command, false))
    }

    override suspend fun send(command: CommandMessage<*>): Boolean {
        return sendAsync(command).asDeferred().await()
    }

    override fun executeAsync(command: CommandMessage<*>): CompletableFuture<CommandResult> {
        return executeAsync(command, CommandReturnType.CommandExecuted)
    }

    override fun executeAsync(
        command: CommandMessage<*>,
        commandReturnType: CommandReturnType
    ): CompletableFuture<CommandResult> {
        val taskCompletionSource = CompletableFuture<CommandResult>()
        try {
            Assert.nonNull(commandResultProcessor, "commandResultProcessor")
            commandResultProcessor.registerProcessingCommand(command, commandReturnType, taskCompletionSource)
            val sendMessageAsync = sendMessageService.sendMessageAsync(buildCommandMessage(command, true))
            sendMessageAsync.exceptionally { ex: Throwable ->
                commandResultProcessor.processFailedSendingCommand(command)
                taskCompletionSource.completeExceptionally(ex)
                null
            }
        } catch (ex: Exception) {
            taskCompletionSource.completeExceptionally(ex)
        }
        return taskCompletionSource
    }

    override suspend fun execute(command: CommandMessage<*>): CommandResult {
        return executeAsync(command).asDeferred().await()
    }

    override suspend fun execute(command: CommandMessage<*>, commandReturnType: CommandReturnType): CommandResult {
        return executeAsync(command, commandReturnType).asDeferred().await()
    }

    protected fun buildCommandMessage(command: CommandMessage<*>, needReply: Boolean): QueueMessage {
        Assert.nonNull(command.aggregateRootId, "aggregateRootId")
        Assert.nonNull(topic, "topic")
        val commandData = serializeService.serialize(command)
        val genericCommandMessage = GenericCommandMessage()
        if (needReply) {
            genericCommandMessage.replyAddress = ReplyUtil.toUri(commandResultProcessor.getBindAddress())
        }
        genericCommandMessage.commandData = commandData
        genericCommandMessage.commandType = command.javaClass.name
        val messageData = serializeService.serialize(genericCommandMessage)
        val queueMessage = QueueMessage()
        queueMessage.topic = topic
        queueMessage.tag = tag
        queueMessage.body = messageData
        queueMessage.routeKey = command.getAggregateRootIdAsString()
        val key = "${command.id}_cmd_agg_${command.getAggregateRootIdAsString()}"
        queueMessage.key = key
        return queueMessage
    }
}