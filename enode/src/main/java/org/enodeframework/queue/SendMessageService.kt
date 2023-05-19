package org.enodeframework.queue

import java.util.concurrent.CompletableFuture

interface SendMessageService {
    fun sendMessageAsync(queueMessage: QueueMessage): CompletableFuture<Boolean>
}
