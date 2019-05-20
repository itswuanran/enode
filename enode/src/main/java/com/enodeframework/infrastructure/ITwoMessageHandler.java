package com.enodeframework.infrastructure;

import com.enodeframework.common.io.AsyncTaskResult;

public interface ITwoMessageHandler<T extends IMessage, E extends IMessage> {
    AsyncTaskResult handleAsync(T message1, E message2);
}