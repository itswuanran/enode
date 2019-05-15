package com.enodeframework.samples.eventhandlers.bank;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.infrastructure.IMessageHandler;
import com.enodeframework.samples.applicationmessages.AccountValidateFailedMessage;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class AccountValidateFailedMessageHandler extends AbstractEventHandler implements IMessageHandler<AccountValidateFailedMessage> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(AccountValidateFailedMessage message) {
        return handleAsyncInternal(message);
    }
}
