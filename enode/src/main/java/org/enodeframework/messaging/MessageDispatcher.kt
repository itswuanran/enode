package org.enodeframework.messaging

import java.util.concurrent.CompletableFuture

interface MessageDispatcher {
    fun dispatchMessageAsync(message: Message): CompletableFuture<Boolean>
    fun dispatchMessagesAsync(messages: List<Message>): CompletableFuture<Boolean>
}