package com.enodeframework.kafka;

import com.enodeframework.queue.IMessageContext;
import com.enodeframework.queue.QueueMessage;
import com.enodeframework.queue.command.CommandListener;
import com.enodeframework.queue.command.ConsumeStatus;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public class KafkaCommandConsumer extends CommandListener implements IMessageListener {
    @Override
    public ConsumeStatus receiveMessage(ConsumerRecord message, IMessageContext context) {
        QueueMessage queueMessage = KafkaTool.covertToQueueMessage(message);
        handle(queueMessage, context);
        return ConsumeStatus.CONSUMESUCCESS;
    }

}
