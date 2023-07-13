package org.enodeframework.kafka.message

import org.enodeframework.queue.QueueMessage
import org.enodeframework.queue.SendMessageResult
import org.enodeframework.queue.SendMessageService
import java.util.concurrent.CompletableFuture

/**
 * @author anruence@gmail.com
 */
class KafkaSendMessageService(private val kafkaProducerHolder: KafkaProducerHolder) : SendMessageService {
    override fun sendMessageAsync(queueMessage: QueueMessage): CompletableFuture<SendMessageResult> {
        return kafkaProducerHolder.send(queueMessage)
    }
}