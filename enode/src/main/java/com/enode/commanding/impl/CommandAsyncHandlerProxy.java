package com.enode.commanding.impl;

import com.enode.commanding.ICommand;
import com.enode.commanding.ICommandAsyncHandler;
import com.enode.commanding.ICommandAsyncHandlerProxy;
import com.enode.common.container.IObjectContainer;
import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IApplicationMessage;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public class CommandAsyncHandlerProxy implements ICommandAsyncHandlerProxy {

    @Autowired
    private IObjectContainer objectContainer;

    private Class handlerType;

    private Object commandHandler;

    private MethodHandle methodHandle;

    private Method method;

    @Override
    public CompletableFuture<AsyncTaskResult<IApplicationMessage>> handleAsync(ICommand command) {
        ICommandAsyncHandler handler = (ICommandAsyncHandler) getInnerObject();
        try {
            return (CompletableFuture<AsyncTaskResult<IApplicationMessage>>) methodHandle.invoke(handler, command);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object getInnerObject() {
        if (commandHandler != null) {
            return commandHandler;
        }
        commandHandler = objectContainer.resolve(handlerType);
        return commandHandler;
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
