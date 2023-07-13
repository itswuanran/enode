package org.enodeframework.kafka.message

import com.google.common.collect.Maps
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.future.await
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.Header
import org.apache.kafka.common.header.internals.RecordHeader
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.extensions.SysProperties
import org.enodeframework.queue.QueueMessage
import org.enodeframework.queue.SendMessageResult
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import java.util.concurrent.CompletableFuture

class KafkaProducerHolder(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val dispatcher: CoroutineDispatcher,
) {

    private val logger = LoggerFactory.getLogger(KafkaProducerHolder::class.java)

    fun send(queueMessage: QueueMessage): CompletableFuture<SendMessageResult> {
        return CoroutineScope(dispatcher).async {
            sendAsync(queueMessage).await()
        }.asCompletableFuture()
    }

    private fun sendAsync(queueMessage: QueueMessage): CompletableFuture<SendMessageResult> {
        val message: ProducerRecord<String, String> = this.covertToProducerRecord(queueMessage)
        return kafkaTemplate.send(message)
            .handle { result: SendResult<String, String>, throwable: Throwable? ->
                if (throwable != null) {
                    logger.error(
                        "Async send message has exception, message: {}",
                        queueMessage,
                        throwable
                    )
                    throw IORuntimeException(throwable)
                }
                if (logger.isDebugEnabled) {
                    logger.debug(
                        "Async send message success, sendResult: {}, message: {}",
                        result,
                        queueMessage
                    )
                }
                val items: MutableMap<String, Any> = Maps.newHashMap()
                items["result"] = result
                SendMessageResult("", items)
            }
    }

    private fun covertToProducerRecord(queueMessage: QueueMessage): ProducerRecord<String, String> {
        val record = ProducerRecord(queueMessage.topic, queueMessage.routeKey, queueMessage.bodyAsStr())
        val mTypeHeader: Header = RecordHeader(SysProperties.MESSAGE_TYPE_KEY, queueMessage.type.toByteArray())
        val tagHeader: Header = RecordHeader(SysProperties.MESSAGE_TAG_KEY, queueMessage.tag.toByteArray())
        record.headers().add(mTypeHeader).add(tagHeader)
        return record
    }
}