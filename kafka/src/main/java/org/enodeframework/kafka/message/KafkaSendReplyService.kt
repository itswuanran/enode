package org.enodeframework.kafka.message

import org.enodeframework.commanding.CommandOptions
import org.enodeframework.common.serializing.SerializeService
import org.enodeframework.messaging.ReplyMessage
import org.enodeframework.queue.MessageTypeCode
import org.enodeframework.queue.QueueMessage
import org.enodeframework.queue.SendMessageResult
import org.enodeframework.queue.SendReplyService
import java.util.concurrent.CompletableFuture

/**
 * @author anruence@gmail.com
 */
class KafkaSendReplyService(
    private val kafkaProducerHolder: KafkaProducerHolder,
    private val commandOptions: CommandOptions,
    private val serializeService: SerializeService
) : SendReplyService {
    override fun send(message: ReplyMessage): CompletableFuture<SendMessageResult> {
        return kafkaProducerHolder.send(buildQueueMessage(message))
    }
    private fun buildQueueMessage(replyMessage: ReplyMessage): QueueMessage {
        val message = replyMessage.asGenericReplyMessage()
        val queueMessage = replyMessage.asPartQueueMessage()
        queueMessage.topic = commandOptions.replyWith(replyMessage.address)
        queueMessage.tag = replyMessage.address
        queueMessage.body = serializeService.serializeBytes(message)
        queueMessage.type = MessageTypeCode.ReplyMessage.value
        return queueMessage
    }
}