package org.enodeframework.queue.command

import kotlinx.coroutines.future.await
import org.enodeframework.commanding.CommandBus
import org.enodeframework.commanding.CommandMessage
import org.enodeframework.commanding.CommandResult
import org.enodeframework.commanding.CommandReturnType
import org.enodeframework.commanding.CommandStatus
import org.enodeframework.common.serializing.SerializeService
import org.enodeframework.common.utils.Assert
import org.enodeframework.queue.MessageTypeCode
import org.enodeframework.queue.QueueMessage
import org.enodeframework.queue.SendMessageResult
import org.enodeframework.queue.SendMessageService
import org.enodeframework.queue.reply.GenericReplyMessage
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
    override fun sendAsync(command: CommandMessage): CompletableFuture<SendMessageResult> {
        return sendMessageService.sendMessageAsync(buildCommandMessage(command, false))
    }

    override suspend fun send(command: CommandMessage): SendMessageResult {
        return sendAsync(command).await()
    }

    override fun executeAsync(command: CommandMessage): CompletableFuture<CommandResult> {
        return executeAsync(command, CommandReturnType.CommandExecuted)
    }

    override fun executeAsync(
        command: CommandMessage, commandReturnType: CommandReturnType
    ): CompletableFuture<CommandResult> {
        val taskCompletionSource = CompletableFuture<CommandResult>()
        try {
            Assert.nonNull(commandResultProcessor, "commandResultProcessor")
            commandResultProcessor.registerProcessingCommand(command, commandReturnType, taskCompletionSource)
            sendMessageService.sendMessageAsync(buildCommandMessage(command, true)).whenComplete { _, ex: Throwable? ->
                if (ex != null) {
                    val replyMessage = GenericReplyMessage()
                    replyMessage.status = CommandStatus.SendFailed.value
                    replyMessage.commandId = command.id
                    replyMessage.aggregateRootId = command.aggregateRootId
                    replyMessage.returnType = commandReturnType.value
                    replyMessage.result = ex.message ?: ""
                    commandResultProcessor.processReplyMessage(replyMessage)
                    taskCompletionSource.completeExceptionally(ex)
                }
            }
        } catch (ex: Exception) {
            taskCompletionSource.completeExceptionally(ex)
        }
        return taskCompletionSource
    }

    override suspend fun execute(command: CommandMessage): CommandResult {
        return executeAsync(command).await()
    }

    override suspend fun execute(command: CommandMessage, commandReturnType: CommandReturnType): CommandResult {
        return executeAsync(command, commandReturnType).await()
    }

    private fun buildCommandMessage(command: CommandMessage, needReply: Boolean): QueueMessage {
        Assert.nonNull(command.aggregateRootId, "aggregateRootId")
        Assert.nonNull(topic, "topic")
        val commandData = serializeService.serialize(command)
        val genericCommandMessage = GenericCommandMessage()
        if (needReply) {
            genericCommandMessage.replyAddress = commandResultProcessor.replyAddress()
        }
        genericCommandMessage.commandData = commandData
        genericCommandMessage.commandType = command.javaClass.name
        val messageData = serializeService.serializeBytes(genericCommandMessage)
        val queueMessage = QueueMessage()
        queueMessage.topic = topic
        queueMessage.tag = tag
        queueMessage.body = messageData
        queueMessage.type = MessageTypeCode.CommandMessage.value
        queueMessage.routeKey = command.aggregateRootId
        val key = "${command.id}_cmd_agg_${command.aggregateRootId}"
        queueMessage.key = key
        return queueMessage
    }
}