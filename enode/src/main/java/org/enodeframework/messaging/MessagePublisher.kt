package org.enodeframework.messaging

import org.enodeframework.queue.SendMessageResult
import java.util.concurrent.CompletableFuture

interface MessagePublisher<TMessage : Message> {
    fun publishAsync(message: TMessage): CompletableFuture<SendMessageResult>
}