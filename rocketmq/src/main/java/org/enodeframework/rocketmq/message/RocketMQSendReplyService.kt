package org.enodeframework.rocketmq.message

import org.enodeframework.common.serializing.SerializeService
import org.enodeframework.common.utils.Assert
import org.enodeframework.messaging.ReplyMessage
import org.enodeframework.queue.MessageTypeCode
import org.enodeframework.queue.QueueMessage
import org.enodeframework.queue.SendMessageResult
import org.enodeframework.queue.SendMessageService
import org.enodeframework.queue.SendReplyService
import org.enodeframework.queue.reply.GenericReplyMessage
import java.util.concurrent.CompletableFuture

/**
 * @author anruence@gmail.com
 */
class RocketMQSendReplyService(
    private val topic: String,
    private val sendMessageService: SendMessageService,
    private val serializeService: SerializeService,
) : SendReplyService {

    override fun send(
        message: ReplyMessage
    ): CompletableFuture<SendMessageResult> {
        Assert.nonNull(topic, "topic")
        return sendMessageService.sendMessageAsync(buildQueueMessage(message))
    }

    private fun buildQueueMessage(replyMessage: ReplyMessage): QueueMessage {
        val message = convertReplyMessage(replyMessage)
        val queueMessage = QueueMessage()
        queueMessage.key = "${message.commandId}_agg_cmd_${message.aggregateRootId}"
        queueMessage.routeKey = message.commandId
        queueMessage.tag = message.address
        queueMessage.topic = topic
        queueMessage.body = serializeService.serialize(message)
        queueMessage.type = MessageTypeCode.ReplyMessage.value
        return queueMessage
    }

    private fun convertReplyMessage(replyMessage: ReplyMessage): GenericReplyMessage {
        val message = GenericReplyMessage()
        message.id = replyMessage.id
        message.result = replyMessage.result
        message.commandId = replyMessage.commandId
        message.aggregateRootId = replyMessage.aggregateRootId
        message.address = replyMessage.address
        message.status = replyMessage.status.value
        message.returnType = replyMessage.returnType.value
        return message
    }
}