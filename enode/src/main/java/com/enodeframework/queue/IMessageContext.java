package com.enodeframework.queue;

public interface IMessageContext {

    void onMessageHandled(QueueMessage message);
}
