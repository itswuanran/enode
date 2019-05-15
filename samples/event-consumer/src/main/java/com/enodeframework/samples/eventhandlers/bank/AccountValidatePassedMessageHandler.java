package com.enodeframework.samples.eventhandlers.bank;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.infrastructure.IMessageHandler;
import com.enodeframework.samples.applicationmessages.AccountValidatePassedMessage;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class AccountValidatePassedMessageHandler extends AbstractEventHandler implements IMessageHandler<AccountValidatePassedMessage> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(AccountValidatePassedMessage message) {
        return handleAsyncInternal(message);
    }
}
