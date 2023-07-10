package org.enodeframework.ons.message

import com.aliyun.openservices.ons.api.Message
import com.aliyun.openservices.ons.api.OnExceptionContext
import com.aliyun.openservices.ons.api.Producer
import com.aliyun.openservices.ons.api.SendCallback
import com.aliyun.openservices.ons.api.SendResult
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.extensions.SysProperties
import org.enodeframework.queue.QueueMessage
import org.enodeframework.queue.SendMessageResult
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

class OnsProducerHolder(private val producer: Producer) {

    private val logger = LoggerFactory.getLogger(OnsProducerHolder::class.java)
    fun send(queueMessage: QueueMessage?): CompletableFuture<SendMessageResult> {
        val future = CompletableFuture<SendMessageResult>()
        val message: Message = this.covertToProducerRecord(queueMessage!!)
        producer.sendAsync(message, object : SendCallback {
            override fun onSuccess(result: SendResult) {
                if (logger.isDebugEnabled) {
                    logger.debug(
                        "Async send message success, sendResult: {}, message: {}",
                        result,
                        message
                    )
                }
                future.complete(SendMessageResult(result.messageId))
            }

            override fun onException(onExceptionContext: OnExceptionContext) {
                future.completeExceptionally(IORuntimeException(onExceptionContext.exception))
                logger.error(
                    "Async send message has exception, message: {}, routingKey: {}",
                    message,
                    message.shardingKey,
                    onExceptionContext.exception
                )
            }
        })
        return future
    }

    private fun covertToProducerRecord(queueMessage: QueueMessage): Message {
        val message = Message(queueMessage.topic, queueMessage.tag, queueMessage.key, queueMessage.body)
        message.shardingKey = queueMessage.routeKey
        message.putUserProperties(SysProperties.MESSAGE_TYPE_KEY, queueMessage.type)
        return message
    }

}
