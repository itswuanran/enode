package org.enodeframework.queue.reply

import org.enodeframework.common.serializing.SerializeService
import org.enodeframework.queue.MessageContext
import org.enodeframework.queue.MessageHandler
import org.enodeframework.queue.QueueMessage
import org.enodeframework.queue.command.CommandResultProcessor
import org.slf4j.LoggerFactory

class DefaultReplyMessageHandler(
    private val commandResultProcessor: CommandResultProcessor, private val serializeService: SerializeService
) : MessageHandler {
    private val logger = LoggerFactory.getLogger(DefaultReplyMessageHandler::class.java)

    override fun handle(queueMessage: QueueMessage, context: MessageContext) {
        logger.info("Received reply message: {}", queueMessage)
        val replyMessage = serializeService.deserialize(queueMessage.body, GenericReplyMessage::class.java)
        commandResultProcessor.processReplyMessage(replyMessage)
    }
}
