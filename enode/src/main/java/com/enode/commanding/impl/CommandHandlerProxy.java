package com.enode.commanding.impl;

import com.enode.commanding.ICommand;
import com.enode.commanding.ICommandContext;
import com.enode.commanding.ICommandHandler;
import com.enode.commanding.ICommandHandlerProxy;
import com.enode.common.container.IObjectContainer;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public class CommandHandlerProxy implements ICommandHandlerProxy {

    @Autowired
    private IObjectContainer objectContainer;

    private Class handlerType;

    private Object commandHandler;

    private MethodHandle methodHandle;

    private Method method;

    @Override
    public CompletableFuture handleAsync(ICommandContext context, ICommand command) {
        ICommandHandler handler = (ICommandHandler) getInnerObject();
        CompletableFuture future = CompletableFuture.supplyAsync(() -> {
            try {
                return methodHandle.invoke(handler, context, command);
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        });
        return future;
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
