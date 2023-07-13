package org.enodeframework.rocketmq.message

import com.google.common.collect.Maps
import org.apache.rocketmq.client.exception.MQClientException
import org.apache.rocketmq.client.producer.MQProducer
import org.apache.rocketmq.client.producer.SendCallback
import org.apache.rocketmq.client.producer.SendResult
import org.apache.rocketmq.client.producer.selector.SelectMessageQueueByHash
import org.apache.rocketmq.common.message.Message
import org.apache.rocketmq.remoting.exception.RemotingException
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.extensions.SysProperties
import org.enodeframework.queue.QueueMessage
import org.enodeframework.queue.SendMessageResult
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

class RocketMQProducerHolder(private val producer: MQProducer) {

    private val logger = LoggerFactory.getLogger(RocketMQProducerHolder::class.java)
    fun send(queueMessage: QueueMessage): CompletableFuture<SendMessageResult> {
        val future = CompletableFuture<SendMessageResult>()
        val message: Message = this.covertToProducerRecord(queueMessage)
        try {
            producer.send(message, SelectMessageQueueByHash(), queueMessage.routeKey, object : SendCallback {
                override fun onSuccess(result: SendResult) {
                    if (logger.isDebugEnabled) {
                        logger.debug("Async send message success, sendResult: {}, message: {}", result, queueMessage)
                    }
                    val items: MutableMap<String, Any> = Maps.newHashMap()
                    items["result"] = result
                    future.complete(SendMessageResult(result.msgId, items))
                }

                override fun onException(ex: Throwable) {
                    future.completeExceptionally(IORuntimeException(ex))
                    logger.error("Async send message has exception, message: {}", queueMessage, ex)
                }
            })
        } catch (ex: MQClientException) {
            future.completeExceptionally(IORuntimeException(ex))
            logger.error("Async send message has exception, message: {}", queueMessage, ex)
        } catch (ex: RemotingException) {
            future.completeExceptionally(IORuntimeException(ex))
            logger.error("Async send message has exception, message: {}", queueMessage, ex)
        } catch (ex: InterruptedException) {
            future.completeExceptionally(ex)
            logger.error("Async send message has exception, message: {}", queueMessage, ex)
        }
        return future
    }

    private fun covertToProducerRecord(queueMessage: QueueMessage): Message {
        val message = Message(queueMessage.topic, queueMessage.tag, queueMessage.key, queueMessage.body)
        message.putUserProperty(SysProperties.MESSAGE_TYPE_KEY, queueMessage.type)
        return message
    }
}
