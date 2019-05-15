package com.enodeframework.infrastructure;

import com.enodeframework.common.io.AsyncTaskResult;

import java.util.concurrent.CompletableFuture;

public interface IMessageHandler<T extends IMessage> {

    CompletableFuture<AsyncTaskResult> handleAsync(T message);
}