package org.enodeframework.messaging.impl;

import org.enodeframework.common.io.Task;
import org.enodeframework.eventing.EventProcessContext;
import org.enodeframework.queue.MessageContext;
import org.enodeframework.queue.QueueMessage;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class DefaultMessageProcessContext implements EventProcessContext {
    protected final QueueMessage queueMessage;
    protected final MessageContext messageContext;

    public DefaultMessageProcessContext(QueueMessage queueMessage, MessageContext messageContext) {
        this.queueMessage = queueMessage;
        this.messageContext = messageContext;
    }

    @Override
    public CompletableFuture<Boolean> notifyEventProcessed() {
        messageContext.onMessageHandled(queueMessage);
        return Task.completedTask;
    }
}
