package com.enodeframework.samples.eventhandlers.bank;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.infrastructure.IMessageHandler;
import com.enodeframework.samples.domain.bank.bankaccount.AccountCreatedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class AccountCreatedEventHandler extends AbstractEventHandler implements IMessageHandler<AccountCreatedEvent> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(AccountCreatedEvent message) {
        return handleAsyncInternal(message);
    }
}
