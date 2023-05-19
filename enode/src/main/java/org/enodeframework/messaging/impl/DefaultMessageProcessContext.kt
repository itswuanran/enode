package org.enodeframework.messaging.impl

import org.enodeframework.common.io.Task
import org.enodeframework.eventing.EventProcessContext
import org.enodeframework.queue.MessageContext
import org.enodeframework.queue.QueueMessage
import java.util.concurrent.CompletableFuture

/**
 * @author anruence@gmail.com
 */
class DefaultMessageProcessContext(
    protected val queueMessage: QueueMessage,
    protected val messageContext: MessageContext
) : EventProcessContext {
    override fun notifyEventProcessed(): CompletableFuture<Boolean> {
        messageContext.onMessageHandled(queueMessage)
        return Task.completedTask
    }
}
