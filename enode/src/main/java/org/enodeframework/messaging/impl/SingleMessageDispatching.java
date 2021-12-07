package org.enodeframework.messaging.impl;

import org.enodeframework.infrastructure.ObjectProxy;
import org.enodeframework.infrastructure.TypeNameProvider;
import org.enodeframework.messaging.Message;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SingleMessageDispatching {
    private final ConcurrentHashMap<String, ObjectProxy> handlerDict;
    private final QueueMessageDispatching queueMessageDispatching;
    private final Message message;

    public SingleMessageDispatching(Message message, QueueMessageDispatching queueMessageDispatching, List<? extends ObjectProxy> handlers, TypeNameProvider typeNameProvider) {
        this.message = message;
        this.queueMessageDispatching = queueMessageDispatching;
        this.handlerDict = new ConcurrentHashMap<>();
        handlers.forEach(x -> handlerDict.putIfAbsent(typeNameProvider.getTypeName(x.getInnerObject().getClass()), x));
    }

    public void removeHandledHandler(String handlerTypeName) {
        if (handlerDict.remove(handlerTypeName) != null) {
            if (handlerDict.isEmpty()) {
                queueMessageDispatching.onMessageHandled(message);
            }
        }
    }

    public Message getMessage() {
        return message;
    }
}
