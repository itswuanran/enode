package com.enodeframework.commanding;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.infrastructure.IApplicationMessage;

import java.util.concurrent.CompletableFuture;

public interface ICommandAsyncHandler<T extends ICommand> {
    /**
     * Handle the given command async.
     *
     * @param command
     * @return
     */
    CompletableFuture<AsyncTaskResult<IApplicationMessage>> handleAsync(T command);
}
