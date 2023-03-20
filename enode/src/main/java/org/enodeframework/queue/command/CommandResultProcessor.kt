package org.enodeframework.queue.command

import io.vertx.core.net.SocketAddress
import org.enodeframework.commanding.CommandMessage
import org.enodeframework.commanding.CommandResult
import org.enodeframework.commanding.CommandReturnType
import java.net.InetSocketAddress
import java.util.concurrent.CompletableFuture

/**
 * @author anruence@gmail.com
 */
interface CommandResultProcessor {
    fun registerProcessingCommand(
        command: CommandMessage<*>,
        commandReturnType: CommandReturnType,
        taskCompletionSource: CompletableFuture<CommandResult>
    )

    fun getBindAddress(): SocketAddress

    fun processFailedSendingCommand(command: CommandMessage<*>)
}