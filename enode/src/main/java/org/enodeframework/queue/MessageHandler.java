package org.enodeframework.queue;

public interface MessageHandler {
    /**
     * message queue handler
     */
    void handle(QueueMessage queueMessage, MessageContext context);
}
