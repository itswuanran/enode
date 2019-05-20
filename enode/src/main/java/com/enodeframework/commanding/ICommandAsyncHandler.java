package com.enodeframework.commanding;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.infrastructure.IApplicationMessage;

public interface ICommandAsyncHandler<T extends ICommand> {
    /**
     * Handle the given command async.
     *
     * @param command
     * @return
     */
    AsyncTaskResult<IApplicationMessage> handleAsync(T command);
}
