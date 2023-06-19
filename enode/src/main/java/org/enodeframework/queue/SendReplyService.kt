package org.enodeframework.queue

import org.enodeframework.messaging.ReplyMessage
import java.util.concurrent.CompletableFuture

interface SendReplyService {
    /**
     * Send command event handle result
     */
    fun send(message: ReplyMessage): CompletableFuture<SendMessageResult>
}