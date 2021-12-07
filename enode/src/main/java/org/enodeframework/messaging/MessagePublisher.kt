package org.enodeframework.messaging

import java.util.concurrent.CompletableFuture

interface MessagePublisher<TMessage : Message> {
    fun publishAsync(message: TMessage): CompletableFuture<Boolean>
}