package com.enode.infrastructure.impl;

import com.enode.infrastructure.IMessageProcessContext;
import com.enode.queue.IMessageContext;
import com.enode.queue.QueueMessage;

public class DefaultMessageProcessContext implements IMessageProcessContext {

    protected final QueueMessage queueMessage;
    protected final IMessageContext messageContext;

    public DefaultMessageProcessContext(QueueMessage queueMessage, IMessageContext messageContext) {
        this.queueMessage = queueMessage;
        this.messageContext = messageContext;
    }

    @Override
    public void notifyMessageProcessed() {
    }
}
