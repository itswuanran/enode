package org.enodeframework.ons.message;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.OnExceptionContext;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.SendCallback;
import com.aliyun.openservices.ons.api.SendResult;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.queue.QueueMessage;
import org.enodeframework.queue.SendMessageResult;
import org.enodeframework.queue.SendMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class OnsSendMessageService implements SendMessageService {

    private static final Logger logger = LoggerFactory.getLogger(OnsSendMessageService.class);

    private final Producer producer;

    public OnsSendMessageService(Producer producer) {
        this.producer = producer;
    }

    @Override
    public CompletableFuture<SendMessageResult> sendMessageAsync(QueueMessage queueMessage) {
        CompletableFuture<SendMessageResult> future = new CompletableFuture<>();
        Message message = OnsTool.covertToProducerRecord(queueMessage);
        producer.sendAsync(message, new SendCallback() {
            @Override
            public void onSuccess(SendResult result) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Async send message success, sendResult: {}, message: {}", result, message);
                }
                future.complete(new SendMessageResult(result.getMessageId(), result));
            }

            @Override
            public void onException(OnExceptionContext onExceptionContext) {
                future.completeExceptionally(new IORuntimeException(onExceptionContext.getException()));
                logger.error("Async send message has exception, message: {}, routingKey: {}", message, message.getShardingKey(), onExceptionContext.getException());
            }
        });
        return future;
    }
}
