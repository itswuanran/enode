package org.enodeframework.queue.command

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.cache.RemovalCause
import org.enodeframework.commanding.CommandConfiguration
import org.enodeframework.commanding.CommandMessage
import org.enodeframework.commanding.CommandResult
import org.enodeframework.commanding.CommandReturnType
import org.enodeframework.commanding.CommandStatus
import org.enodeframework.common.exception.DuplicateCommandRegisterException
import org.enodeframework.common.extensions.SystemClock
import org.enodeframework.common.scheduling.ScheduleService
import org.enodeframework.common.scheduling.Worker
import org.enodeframework.common.serializing.SerializeService
import org.enodeframework.queue.reply.GenericReplyMessage
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * @author anruence@gmail.com
 */
class DefaultCommandResultProcessor(
    private val scheduleService: ScheduleService,
    private val serializeService: SerializeService,
    private val commandConfiguration: CommandConfiguration,
    private val completionSourceTimeout: Int
) : CommandResultProcessor {
    private val scanExpireCommandTaskName: String =
        "CleanTimeoutCommandTask_" + SystemClock.now() + Random().nextInt(5000)
    private val commandTaskDict: Cache<String, CommandTaskCompletionSource>
    private val commandExecutedMessageLocalQueue: BlockingQueue<GenericReplyMessage>
    private val commandExecutedMessageWorker: Worker

    override fun registerProcessingCommand(
        command: CommandMessage,
        commandReturnType: CommandReturnType,
        taskCompletionSource: CompletableFuture<CommandResult>
    ) {
        if (commandTaskDict.asMap().putIfAbsent(
                command.id,
                CommandTaskCompletionSource(command.aggregateRootId, commandReturnType, taskCompletionSource)
            ) != null
        ) {
            throw DuplicateCommandRegisterException(
                "Duplicate processing command registration, type:${command.javaClass.name}, id:${command.id}"
            )
        }
    }

    override fun processReplyMessage(replyMessage: GenericReplyMessage) {
        val code = replyMessage.returnType
        if (replyMessage.status == CommandStatus.SendFailed.value) {
            processFailedSendingCommand(replyMessage)
            return
        }
        if (code == CommandReturnType.CommandExecuted.value || code == CommandReturnType.EventHandled.value) {
            commandExecutedMessageLocalQueue.add(replyMessage)
        }
    }

    override fun ReplyAddress(): String {
        return "enode://${commandConfiguration.host}:${commandConfiguration.port}"
    }

    fun start() {
        commandExecutedMessageWorker.start()
        scheduleService.startTask(
            scanExpireCommandTaskName, { commandTaskDict.cleanUp() }, completionSourceTimeout, completionSourceTimeout
        )
    }

    fun stop() {
        scheduleService.stopTask(scanExpireCommandTaskName)
        commandExecutedMessageWorker.stop()
    }

    private fun processExecutedCommandMessage(message: GenericReplyMessage) {
        val code = message.returnType
        if (code == CommandReturnType.CommandExecuted.value) {
            processExecutedCommandMessageInternal(message.asCommandResult())
        } else if (code == CommandReturnType.EventHandled.value) {
            processDomainEventHandledMessage(message)
        }
    }

    private fun processExecutedCommandMessageInternal(commandResult: CommandResult) {
        val commandTaskCompletionSource = commandTaskDict.asMap()[commandResult.commandId]
        if (commandTaskCompletionSource == null) {
            if (logger.isDebugEnabled) {
                logger.debug(
                    "Command result return, {}, but commandTaskCompletionSource maybe timeout expired.",
                    serializeService.serialize(
                        commandResult
                    )
                )
            }
            return
        }
        if (commandTaskCompletionSource.commandReturnType == CommandReturnType.CommandExecuted) {
            commandTaskDict.asMap().remove(commandResult.commandId)
            if (commandTaskCompletionSource.taskCompletionSource.complete(commandResult)) {
                if (logger.isDebugEnabled) {
                    logger.debug("Command result return CommandExecuted, {}", serializeService.serialize(commandResult))
                }
            }
        } else if (commandTaskCompletionSource.commandReturnType == CommandReturnType.EventHandled) {
            if (CommandStatus.Failed == commandResult.status || CommandStatus.NoChange == commandResult.status) {
                commandTaskDict.asMap().remove(commandResult.commandId)
                if (commandTaskCompletionSource.taskCompletionSource.complete(commandResult)) {
                    if (logger.isDebugEnabled) {
                        logger.debug(
                            "Command result return EventHandled, {}", serializeService.serialize(commandResult)
                        )
                    }
                }
            }
        }
    }

    private fun processTimeoutCommand(commandId: String, commandTaskCompletionSource: CommandTaskCompletionSource?) {
        if (commandTaskCompletionSource != null) {
            logger.error("Wait command notify timeout, commandId: {}", commandId)
            val commandResult = CommandResult(
                CommandStatus.Failed,
                commandId,
                commandTaskCompletionSource.aggregateRootId,
                "Wait command notify timeout.",
            )
            // 任务超时失败
            commandTaskCompletionSource.taskCompletionSource.complete(commandResult)
        }
    }

    fun processFailedSendingCommand(command: GenericReplyMessage) {
        val commandTaskCompletionSource = commandTaskDict.asMap().remove(command.commandId)
        if (commandTaskCompletionSource != null) {
            val commandResult = CommandResult(
                CommandStatus.Failed,
                command.commandId,
                command.aggregateRootId,
                "Failed to send the command.",
            )
            // 发送失败消息
            commandTaskCompletionSource.taskCompletionSource.complete(commandResult)
        }
    }

    private fun processDomainEventHandledMessage(message: GenericReplyMessage) {
        val commandTaskCompletionSource = commandTaskDict.asMap()[message.commandId]
        if (commandTaskCompletionSource != null) {
            if (CommandReturnType.EventHandled != commandTaskCompletionSource.commandReturnType) {
                logger.warn("event arrived early than command: {}", serializeService.serialize(message))
                return
            }
            commandTaskDict.asMap().remove(message.commandId)
            val commandResult = CommandResult(
                CommandStatus.Success,
                message.commandId,
                message.aggregateRootId,
                message.result,
            )
            commandTaskCompletionSource.taskCompletionSource.complete(commandResult)
            if (logger.isDebugEnabled) {
                logger.debug("DomainEvent result return, {}", serializeService.serialize(message))
            }
        }
    }

    private val logger = LoggerFactory.getLogger(DefaultCommandResultProcessor::class.java)

    init {
        commandTaskDict = CacheBuilder.newBuilder().removalListener { notification ->
            if (notification.cause == RemovalCause.EXPIRED) {
                processTimeoutCommand(notification.key!!, notification.value)
            }
        }.expireAfterWrite(completionSourceTimeout.toLong(), TimeUnit.MILLISECONDS).build()
        commandExecutedMessageLocalQueue = LinkedBlockingQueue()
        commandExecutedMessageWorker = Worker("ProcessExecutedCommandMessage") {
            processExecutedCommandMessage(
                commandExecutedMessageLocalQueue.take()
            )
        }
    }
}