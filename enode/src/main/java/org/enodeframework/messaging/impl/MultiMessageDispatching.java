package org.enodeframework.messaging.impl;

import org.enodeframework.infrastructure.ObjectProxy;
import org.enodeframework.infrastructure.TypeNameProvider;
import org.enodeframework.messaging.Message;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MultiMessageDispatching {
    private final Message[] messages;
    private final ConcurrentMap<String, ObjectProxy> handlerDict;
    private final RootDispatching rootDispatching;

    public MultiMessageDispatching(List<? extends Message> messages, List<? extends ObjectProxy> handlers, RootDispatching rootDispatching, TypeNameProvider typeNameProvider) {
        this.messages = messages.toArray(new Message[0]);
        handlerDict = new ConcurrentHashMap<>();
        handlers.forEach(x -> handlerDict.putIfAbsent(typeNameProvider.getTypeName(x.getInnerObject().getClass()), x));
        this.rootDispatching = rootDispatching;
        this.rootDispatching.addChildDispatching(this);
    }

    public Message[] getMessages() {
        return messages;
    }

    public void removeHandledHandler(String handlerTypeName) {
        if (handlerDict.remove(handlerTypeName) != null) {
            if (handlerDict.isEmpty()) {
                rootDispatching.onChildDispatchingFinished(this);
            }
        }
    }
}
