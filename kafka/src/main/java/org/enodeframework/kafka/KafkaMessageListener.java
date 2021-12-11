package org.enodeframework.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.enodeframework.queue.MessageHandler;
import org.enodeframework.queue.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;

import java.util.Map;

/**
 * @author anruence@gmail.com
 */
public class KafkaMessageListener implements AcknowledgingMessageListener<String, String> {

    private static final Logger logger = LoggerFactory.getLogger(KafkaMessageListener.class);

    private final Map<Character, MessageHandler> messageHandlerMap;

    public KafkaMessageListener(Map<Character, MessageHandler> messageHandlerMap) {
        this.messageHandlerMap = messageHandlerMap;
    }

    /**
     * Invoked with data from kafka.
     *
     * @param data           the data to be processed.
     * @param acknowledgment the acknowledgment.
     */
    @Override
    public void onMessage(ConsumerRecord<String, String> data, Acknowledgment acknowledgment) {
        QueueMessage queueMessage = this.covertToQueueMessage(data);
        MessageHandler messageHandler = messageHandlerMap.get(queueMessage.getType());
        if (messageHandler == null) {
            logger.error("No messageHandler for message: {}.", queueMessage);
            return;
        }
        messageHandler.handle(queueMessage, context -> {
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        });
    }

    private QueueMessage covertToQueueMessage(ConsumerRecord<String, String> record) {
        QueueMessage queueMessage = new QueueMessage();
        int length = record.value().length();
        queueMessage.setBody(record.value().substring(0, length - 2));
        queueMessage.setType(record.value().charAt(length - 1));
        queueMessage.setTopic(record.topic());
        queueMessage.setRouteKey(record.key());
        queueMessage.setKey(record.key());
        return queueMessage;
    }
}
