package org.enodeframework.ons.message;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import org.enodeframework.common.io.Task;
import org.enodeframework.queue.MessageHandler;
import org.enodeframework.queue.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @author anruence@gmail.com
 */
public class OnsMessageListener implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(OnsMessageListener.class);

    private final Map<Character, MessageHandler> messageHandlerMap;

    public OnsMessageListener(Map<Character, MessageHandler> messageHandlerMap) {
        this.messageHandlerMap = messageHandlerMap;
    }

    @Override
    public Action consume(Message msg, ConsumeContext consumeContext) {
        QueueMessage queueMessage = OnsTool.covertToQueueMessage(msg);
        MessageHandler messageHandler = messageHandlerMap.get(queueMessage.getType());
        if (messageHandler == null) {
            logger.error("No messageHandler for message: {}.", queueMessage);
            return Action.CommitMessage;
        }
        CountDownLatch latch = new CountDownLatch(1);
        messageHandler.handle(queueMessage, message -> {
            latch.countDown();
        });
        Task.await(latch);
        return Action.CommitMessage;
    }
}