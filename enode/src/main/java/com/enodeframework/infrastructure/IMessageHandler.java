package com.enodeframework.infrastructure;

import com.enodeframework.common.io.AsyncTaskResult;

public interface IMessageHandler<T extends IMessage> {
    AsyncTaskResult handleAsync(T message);
}