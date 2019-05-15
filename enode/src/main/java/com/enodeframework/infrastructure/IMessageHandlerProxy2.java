package com.enodeframework.infrastructure;

import com.enodeframework.common.io.AsyncTaskResult;

import java.util.concurrent.CompletableFuture;

public interface IMessageHandlerProxy2 extends IObjectProxy, MethodInvocation {
    CompletableFuture<AsyncTaskResult> handleAsync(IMessage message1, IMessage message2);
}
