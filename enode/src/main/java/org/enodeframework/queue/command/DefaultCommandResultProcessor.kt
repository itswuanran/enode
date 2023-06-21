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
    private val commandExecutedMessageLocalQueue: BlockingQueue<CommandResult>
    private val domainEventHandledMessageLocalQueue: BlockingQueue<GenericReplyMessage>
    private val commandExecutedMessageWorker: Worker
    private val domainEventHandledMessageWorker: Worker

    override fun registerProcessingCommand(
        command: CommandMessage,
        commandReturnType: CommandReturnType,
        taskCompletionSource: CompletableFuture<CommandResult>
    ) {
        if (commandTaskDict.asMap().putIfAbsent(
                command.id, CommandTaskCompletionSource(
                    command.aggregateRootId,
                    commandReturnType,
                    taskCompletionSource
                )
            ) != null
        ) {
            throw DuplicateCommandRegisterException(
                "Duplicate processing command registration, type:${command.javaClass.name}, id:${command.id}"
            )
        }
    }

    override fun processReplyMessage(replyMessage: GenericReplyMessage) {
        val code = replyMessage.returnType
        if (code == CommandReturnType.CommandExecuted.value) {
            val result = replyMessage.asCommandResult()
            commandExecutedMessageLocalQueue.add(result)
        } else if (code == CommandReturnType.EventHandled.value) {
            domainEventHandledMessageLocalQueue.add(replyMessage)
        }
    }

    override fun ReplyAddress(): String {
        return "enode://${commandConfiguration.host}:${commandConfiguration.port}"
    }

    fun start() {
        commandExecutedMessageWorker.start()
        domainEventHandledMessageWorker.start()
        scheduleService.startTask(
            scanExpireCommandTaskName,
            { commandTaskDict.cleanUp() },
            completionSourceTimeout,
            completionSourceTimeout
        )
    }

    fun stop() {
        scheduleService.stopTask(scanExpireCommandTaskName)
        commandExecutedMessageWorker.stop()
        domainEventHandledMessageWorker.stop()
    }


    /**
     * https://stackoverflow.com/questions/10626720/guava-cachebuilder-removal-listener
     * Caches built with CacheBuilder do not perform cleanup and evict values "automatically," or instantly
     * after a value expires, or anything of the sort. Instead, it performs small amounts of maintenance
     * during write operations, or during occasional read operations if writes are rare.
     *
     *
     * The reason for this is as follows: if we wanted to perform Cache maintenance continuously, we would need
     * to create a thread, and its operations would be competing with user operations for shared locks.
     * Additionally, some environments restrict the creation of threads, which would make CacheBuilder unusable in that environment.
     */
    private fun processExecutedCommandMessage(commandResult: CommandResult) {
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
                            "Command result return EventHandled, {}",
                            serializeService.serialize(commandResult)
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

    fun processFailedSendingCommand(command: CommandMessage) {
        val commandTaskCompletionSource = commandTaskDict.asMap().remove(command.id)
        if (commandTaskCompletionSource != null) {
            val commandResult = CommandResult(
                CommandStatus.Failed,
                command.id,
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
                "",
            )
            commandTaskCompletionSource.taskCompletionSource.complete(commandResult)
            if (logger.isDebugEnabled) {
                logger.debug("DomainEvent result return, {}", serializeService.serialize(message))
            }
        }
    }

    private val logger = LoggerFactory.getLogger(DefaultCommandResultProcessor::class.java)

    init {
        commandTaskDict = CacheBuilder.newBuilder()
            .removalListener { notification ->
                if (notification.cause == RemovalCause.EXPIRED) {
                    processTimeoutCommand(notification.key!!, notification.value)
                }
            }.expireAfterWrite(completionSourceTimeout.toLong(), TimeUnit.MILLISECONDS)
            .build()
        commandExecutedMessageLocalQueue = LinkedBlockingQueue()
        domainEventHandledMessageLocalQueue = LinkedBlockingQueue()
        commandExecutedMessageWorker = Worker("ProcessExecutedCommandMessage") {
            processExecutedCommandMessage(
                commandExecutedMessageLocalQueue.take()
            )
        }
        domainEventHandledMessageWorker = Worker("ProcessDomainEventHandledMessage") {
            processDomainEventHandledMessage(
                domainEventHandledMessageLocalQueue.take()
            )
        }
    }
}