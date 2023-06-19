package org.enodeframework.queue.command

import org.enodeframework.commanding.CommandMessage
import org.enodeframework.commanding.CommandResult
import org.enodeframework.commanding.CommandReturnType
import org.enodeframework.messaging.ReplyMessage
import org.enodeframework.queue.reply.GenericReplyMessage
import java.util.concurrent.CompletableFuture

/**
 * @author anruence@gmail.com
 */
interface CommandResultProcessor {
    fun registerProcessingCommand(
        command: CommandMessage,
        commandReturnType: CommandReturnType,
        taskCompletionSource: CompletableFuture<CommandResult>
    )

    fun processReplyMessage(replyMessage: GenericReplyMessage)

    fun uniqueReplyAddress(): String
}