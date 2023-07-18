package org.enodeframework.kafka.message

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.header.Header
import org.enodeframework.common.extensions.SysProperties
import org.enodeframework.queue.MessageHandlerHolder
import org.enodeframework.queue.QueueMessage
import org.springframework.kafka.listener.AcknowledgingConsumerAwareMessageListener
import org.springframework.kafka.listener.AcknowledgingMessageListener
import org.springframework.kafka.listener.BatchAcknowledgingConsumerAwareMessageListener
import org.springframework.kafka.listener.BatchAcknowledgingMessageListener
import org.springframework.kafka.listener.BatchConsumerAwareMessageListener
import org.springframework.kafka.listener.BatchMessageListener
import org.springframework.kafka.listener.ConsumerAwareMessageListener
import org.springframework.kafka.listener.MessageListener
import org.springframework.kafka.support.Acknowledgment
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.CountDownLatch

/**
 * @author anruence@gmail.com
 */

class KafkaMessageListener(private val messageHandlerHolder: MessageHandlerHolder) : MessageListener<String, String> {

    override fun onMessage(data: ConsumerRecord<String, String>) {
        onSingleMessage(messageHandlerHolder, data, null)
    }
}

class KafkaAcknowledgingMessageListener(private val messageHandlerHolder: MessageHandlerHolder) :
    AcknowledgingMessageListener<String, String> {

    override fun onMessage(data: ConsumerRecord<String, String>, acknowledgment: Acknowledgment?) {
        onSingleMessage(messageHandlerHolder, data, acknowledgment)
    }
}

class KafkaAcknowledgingConsumerAwareMessageListener(private val messageHandlerHolder: MessageHandlerHolder) :
    AcknowledgingConsumerAwareMessageListener<String, String> {

    override fun onMessage(
        data: ConsumerRecord<String, String>, acknowledgment: Acknowledgment?, consumer: Consumer<*, *>
    ) {
        onSingleMessage(messageHandlerHolder, data, acknowledgment)
    }
}

class KafkaConsumerAwareMessageListener(private val messageHandlerHolder: MessageHandlerHolder) :
    ConsumerAwareMessageListener<String, String> {
    override fun onMessage(data: ConsumerRecord<String, String>, consumer: Consumer<*, *>) {
        onSingleMessage(messageHandlerHolder, data, null)
    }
}

class KafkaBatchAcknowledgingMessageListener(private val messageHandlerHolder: MessageHandlerHolder) :
    BatchAcknowledgingMessageListener<String, String> {

    override fun onMessage(
        data: List<ConsumerRecord<String, String>>,
        acknowledgment: Acknowledgment?,
    ) {
        onBatchMessage(messageHandlerHolder, data, acknowledgment)
    }
}

class KafkaBatchAcknowledgingConsumerAwareMessageListener(private val messageHandlerHolder: MessageHandlerHolder) :
    BatchAcknowledgingConsumerAwareMessageListener<String, String> {

    override fun onMessage(
        data: MutableList<ConsumerRecord<String, String>>, acknowledgment: Acknowledgment?, consumer: Consumer<*, *>
    ) {
        onBatchMessage(messageHandlerHolder, data, acknowledgment)
    }
}

class KafkaBatchConsumerAwareMessageListener(private val messageHandlerHolder: MessageHandlerHolder) :
    BatchConsumerAwareMessageListener<String, String> {

    override fun onMessage(data: MutableList<ConsumerRecord<String, String>>, consumer: Consumer<*, *>) {
        onBatchMessage(messageHandlerHolder, data, null)
    }
}

class KafkaBatchMessageListener(private val messageHandlerHolder: MessageHandlerHolder) :
    BatchMessageListener<String, String> {

    override fun onMessage(data: MutableList<ConsumerRecord<String, String>>) {
        onBatchMessage(messageHandlerHolder, data, null)
    }
}


private fun covertToQueueMessage(record: ConsumerRecord<String, String>): QueueMessage {
    val mType = Optional.ofNullable(record.headers().lastHeader(SysProperties.MESSAGE_TYPE_KEY))
        .map { x: Header -> String(x.value()) }.orElse("")
    val tag = Optional.ofNullable(record.headers().lastHeader(SysProperties.MESSAGE_TAG_KEY))
        .map { x: Header -> String(x.value()) }.orElse("")
    val queueMessage = QueueMessage()
    queueMessage.body = record.value().toByteArray(StandardCharsets.UTF_8)
    queueMessage.type = mType
    queueMessage.tag = tag
    queueMessage.topic = record.topic()
    queueMessage.routeKey = record.key()
    queueMessage.key = record.key()
    return queueMessage
}

fun onSingleMessage(
    messageHandlerHolder: MessageHandlerHolder,
    data: ConsumerRecord<String, String>,
    acknowledgment: Acknowledgment?,
) {
    val queueMessage = covertToQueueMessage(data)
    val messageHandler = messageHandlerHolder.chooseMessageHandler(queueMessage.type)
    messageHandler.handle(queueMessage) {
        acknowledgment?.acknowledge()
    }
}

fun onBatchMessage(
    messageHandlerHolder: MessageHandlerHolder,
    data: List<ConsumerRecord<String, String>>,
    acknowledgment: Acknowledgment?,
) {
    val latch = CountDownLatch(data.size)
    data.forEach { message: ConsumerRecord<String, String> ->
        val queueMessage = covertToQueueMessage(message)
        val messageHandler = messageHandlerHolder.chooseMessageHandler(queueMessage.type)
        messageHandler.handle(queueMessage) { latch.countDown() }
    }
    latch.await()
    acknowledgment?.acknowledge()
}