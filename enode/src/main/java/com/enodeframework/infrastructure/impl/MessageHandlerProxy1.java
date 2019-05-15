package com.enodeframework.infrastructure.impl;

import com.enodeframework.common.container.IObjectContainer;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.infrastructure.IMessage;
import com.enodeframework.infrastructure.IMessageHandler;
import com.enodeframework.infrastructure.IMessageHandlerProxy1;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public class MessageHandlerProxy1 implements IMessageHandlerProxy1 {

    @Autowired
    private IObjectContainer objectContainer;

    private Class handlerType;

    private Object handler;

    private MethodHandle methodHandle;

    private Method method;

    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(IMessage message) {
        IMessageHandler handler = (IMessageHandler) getInnerObject();
        try {
            return (CompletableFuture<AsyncTaskResult>) methodHandle.invoke(handler, message);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @Override
    public Object getInnerObject() {
        if (handler != null) {
            return handler;
        }
        handler = objectContainer.resolve(handlerType);
        return handler;
    }

    @Override
    public void setHandlerType(Class handlerType) {
        this.handlerType = handlerType;
    }

    @Override
    public void setMethodHandle(MethodHandle methodHandle) {
        this.methodHandle = methodHandle;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public void setMethod(Method method) {
        this.method = method;
    }

}
