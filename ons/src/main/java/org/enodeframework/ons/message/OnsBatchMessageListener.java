package org.enodeframework.ons.message;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.batch.BatchMessageListener;
import org.enodeframework.common.io.Task;
import org.enodeframework.queue.MessageHandler;
import org.enodeframework.queue.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @author anruence@gmail.com
 */
public class OnsBatchMessageListener implements BatchMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(OnsBatchMessageListener.class);

    private final Map<Character, MessageHandler> messageHandlerMap;

    public OnsBatchMessageListener(Map<Character, MessageHandler> messageHandlerMap) {
        this.messageHandlerMap = messageHandlerMap;
    }

    @Override
    public Action consume(List<Message> messages, ConsumeContext context) {
        CountDownLatch latch = new CountDownLatch(messages.size());
        messages.forEach(msg -> {
            QueueMessage queueMessage = OnsTool.covertToQueueMessage(msg);
            MessageHandler messageHandler = messageHandlerMap.get(queueMessage.getType());
            if (messageHandler == null) {
                logger.error("No messageHandler for message: {}.", queueMessage);
                latch.countDown();
                return;
            }
            messageHandler.handle(queueMessage, message -> {
                latch.countDown();
            });
        });
        Task.await(latch);
        return Action.CommitMessage;
    }
}