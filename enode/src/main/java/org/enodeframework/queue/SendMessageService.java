package org.enodeframework.queue;

import java.util.concurrent.CompletableFuture;

public interface SendMessageService {
    CompletableFuture<Boolean> sendMessageAsync(QueueMessage queueMessage);
}
