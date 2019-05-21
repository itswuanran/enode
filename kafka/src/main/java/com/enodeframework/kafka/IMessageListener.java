package com.enodeframework.kafka;

import com.enodeframework.queue.IMessageContext;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface IMessageListener {

    ConsumeStatus receiveMessage(ConsumerRecord message, IMessageContext context);

}
