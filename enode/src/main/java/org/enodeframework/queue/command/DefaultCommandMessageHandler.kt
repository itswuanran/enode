package org.enodeframework.queue.command

import com.google.common.base.Strings
import org.enodeframework.commanding.CommandMessage
import org.enodeframework.commanding.CommandProcessor
import org.enodeframework.commanding.ProcessingCommand
import org.enodeframework.commanding.impl.DefaultCommandExecuteContext
import org.enodeframework.common.extensions.SysProperties
import org.enodeframework.common.serializing.SerializeService
import org.enodeframework.domain.AggregateStorage
import org.enodeframework.domain.Repository
import org.enodeframework.infrastructure.TypeNameProvider
import org.enodeframework.queue.MessageContext
import org.enodeframework.queue.MessageHandler
import org.enodeframework.queue.QueueMessage
import org.enodeframework.queue.SendReplyService
import org.slf4j.LoggerFactory

class DefaultCommandMessageHandler(
    private val sendReplyService: SendReplyService,
    private val typeNameProvider: TypeNameProvider,
    private val commandProcessor: CommandProcessor,
    private val repository: Repository,
    private val aggregateRootStorage: AggregateStorage,
    private val serializeService: SerializeService
) : MessageHandler {
    private val logger = LoggerFactory.getLogger(DefaultCommandMessageHandler::class.java)

    override fun handle(queueMessage: QueueMessage, context: MessageContext) {
        logger.info("Received command message: {}", queueMessage)
        val commandMessage = serializeService.deserializeBytes(queueMessage.body, GenericCommandMessage::class.java)
        val commandType = typeNameProvider.getType(commandMessage.commandType)
        val command = serializeService.deserialize(commandMessage.commandData, commandType) as CommandMessage
        val commandExecuteContext = DefaultCommandExecuteContext(
            repository, aggregateRootStorage, queueMessage, context, commandMessage, sendReplyService
        )
        val commandItems: MutableMap<String, Any> = HashMap()
        val uri = commandMessage.replyAddress
        if (!Strings.isNullOrEmpty(uri)) {
            commandItems[SysProperties.ITEMS_COMMAND_REPLY_ADDRESS_KEY] = uri
        }
        commandProcessor.process(ProcessingCommand(command, commandExecuteContext, commandItems))
    }
}
