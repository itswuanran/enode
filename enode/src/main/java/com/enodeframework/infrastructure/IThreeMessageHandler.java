package com.enodeframework.infrastructure;

import com.enodeframework.common.io.AsyncTaskResult;

public interface IThreeMessageHandler<T extends IMessage, E extends IMessage, D extends IMessage> {
    AsyncTaskResult handleAsync(T message1, E message2, D message3);
}