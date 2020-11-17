package org.enodeframework.queue;

public interface IMessageHandler {
    /**
     * message queue handler
     */
    void handle(QueueMessage queueMessage, IMessageContext context);
}
