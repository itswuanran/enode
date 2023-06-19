package org.enodeframework.queue.reply

import org.enodeframework.messaging.MessagePublisher
import org.enodeframework.messaging.ReplyMessage
import org.enodeframework.queue.SendMessageResult
import org.enodeframework.queue.SendReplyService
import java.util.concurrent.CompletableFuture

class DefaultReplyMessagePublisher(private val sendReplyService: SendReplyService) : MessagePublisher<ReplyMessage> {

    override fun publishAsync(message: ReplyMessage): CompletableFuture<SendMessageResult> {
        return sendReplyService.send(message)
    }
}
