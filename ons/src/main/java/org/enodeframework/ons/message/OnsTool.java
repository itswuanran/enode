package org.enodeframework.ons.message;

import com.aliyun.openservices.ons.api.Message;
import org.enodeframework.queue.QueueMessage;

import java.nio.charset.StandardCharsets;

/**
 * @author anruence@gmail.com
 */
public class OnsTool {

    public static QueueMessage covertToQueueMessage(Message messageExt) {
        QueueMessage queueMessage = new QueueMessage();
        String value = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        int length = value.length();
        // 格式为{}|1
        queueMessage.setBody(value.substring(0, length - 2));
        queueMessage.setType(value.charAt(length - 1));
        queueMessage.setTopic(messageExt.getTopic());
        queueMessage.setTag(messageExt.getTag());
        queueMessage.setRouteKey(messageExt.getShardingKey());
        queueMessage.setKey(messageExt.getKey());
        return queueMessage;
    }

    public static Message covertToProducerRecord(QueueMessage queueMessage) {
        Message message = new Message(queueMessage.getTopic(), queueMessage.getTag(), queueMessage.getKey(), queueMessage.getBodyAndType().getBytes(StandardCharsets.UTF_8));
        message.setShardingKey(queueMessage.getRouteKey());
        return message;
    }
}
