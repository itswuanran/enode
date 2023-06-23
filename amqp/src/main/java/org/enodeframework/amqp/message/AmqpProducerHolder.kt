package org.enodeframework.amqp.message

import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.extensions.SysProperties
import org.enodeframework.queue.QueueMessage
import org.enodeframework.queue.SendMessageResult
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.AsyncAmqpTemplate
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties
import java.util.concurrent.CompletableFuture

class AmqpProducerHolder(private val asyncAmqpTemplate: AsyncAmqpTemplate) {

    private val logger = LoggerFactory.getLogger(AmqpProducerHolder::class.java)

    fun send(queueMessage: QueueMessage): CompletableFuture<SendMessageResult> {
        return asyncAmqpTemplate.sendAndReceive(
            queueMessage.topic,
            queueMessage.routeKey,
            this.covertToAmqpMessage(queueMessage)
        ).exceptionally { throwable: Throwable? ->
            logger.error(
                "Async send message failed, error: {}, message: {}",
                throwable,
                queueMessage
            )
            throw IORuntimeException(throwable)
        }.thenApply { x: Message ->
            if (logger.isDebugEnabled) {
                logger.debug(
                    "Async send message success, sendResult: {}, message: {}",
                    x.toString(),
                    queueMessage
                )
            }
            SendMessageResult(x.messageProperties.messageId, x.messageProperties.headers)
        }
    }

    private fun covertToAmqpMessage(queueMessage: QueueMessage): Message {
        val props = MessageProperties()
        props.consumerQueue = queueMessage.topic
        props.consumerTag = queueMessage.tag
        props.receivedRoutingKey = queueMessage.routeKey
        props.messageId = queueMessage.key
        props.setHeader(SysProperties.MESSAGE_TYPE_KEY, queueMessage.type)
        props.setHeader(SysProperties.MESSAGE_TAG_KEY, queueMessage.tag)
        return Message(queueMessage.body, props)
    }
}
