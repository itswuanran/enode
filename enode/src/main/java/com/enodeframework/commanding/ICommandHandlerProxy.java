package com.enodeframework.commanding;

import com.enodeframework.infrastructure.IObjectProxy;
import com.enodeframework.infrastructure.MethodInvocation;

import java.util.concurrent.CompletableFuture;

public interface ICommandHandlerProxy extends IObjectProxy, MethodInvocation {
    CompletableFuture<Void> handleAsync(ICommandContext context, ICommand command);
}
