package org.enodeframework.amqp.message

import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.extensions.SysProperties
import org.enodeframework.queue.QueueMessage
import org.enodeframework.queue.SendMessageResult
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties
import java.util.concurrent.CompletableFuture

class AmqpProducerHolder(private val asyncAmqpTemplate: AmqpTemplate) {

    private val logger = LoggerFactory.getLogger(AmqpProducerHolder::class.java)

    fun send(queueMessage: QueueMessage): CompletableFuture<SendMessageResult> {
        return CompletableFuture.supplyAsync {
            asyncAmqpTemplate.send(
                queueMessage.topic,
                "${queueMessage.type}.${queueMessage.tag}",
                this.covertToAmqpMessage(queueMessage)
            )
        }.exceptionally { throwable: Throwable? ->
            logger.error(
                "Async send message failed, error: {}, message: {}",
                throwable,
                queueMessage
            )
            throw IORuntimeException(throwable)
        }.thenApply {
            if (logger.isDebugEnabled) {
                logger.debug(
                    "Async send message success, message: {}", queueMessage
                )
            }
            SendMessageResult("")
        }
    }

    private fun covertToAmqpMessage(queueMessage: QueueMessage): Message {
        val props = MessageProperties()
        props.messageId = queueMessage.key
        props.consumerQueue = queueMessage.topic
        props.consumerTag = queueMessage.tag
        props.setHeader(SysProperties.MESSAGE_TYPE_KEY, queueMessage.type)
        props.setHeader(SysProperties.MESSAGE_TAG_KEY, queueMessage.tag)
        return Message(queueMessage.body, props)
    }
}
