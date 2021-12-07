package org.enodeframework.messaging.impl;

import org.enodeframework.messaging.Message;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class QueueMessageDispatching {
    private final DefaultMessageDispatcher dispatcher;
    private final RootDispatching rootDispatching;
    private final ConcurrentLinkedQueue<Message> messageQueue;

    public QueueMessageDispatching(DefaultMessageDispatcher dispatcher, RootDispatching rootDispatching, List<? extends Message> messages) {
        this.dispatcher = dispatcher;
        messageQueue = new ConcurrentLinkedQueue<>();
        messageQueue.addAll(messages);
        this.rootDispatching = rootDispatching;
        this.rootDispatching.addChildDispatching(this);
    }

    public Message dequeueMessage() {
        return messageQueue.poll();
    }

    public void onMessageHandled(Message message) {
        Message nextMessage = dequeueMessage();
        if (nextMessage == null) {
            rootDispatching.onChildDispatchingFinished(this);
            return;
        }
        dispatcher.dispatchSingleMessage(nextMessage, this);
    }
}
