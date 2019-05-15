package com.enodeframework.samples.eventhandlers.bank;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.infrastructure.IMessageHandler;
import com.enodeframework.samples.domain.bank.bankaccount.InsufficientBalanceException;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class InsufficientBalanceExceptionHandler extends AbstractEventHandler implements IMessageHandler<InsufficientBalanceException> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(InsufficientBalanceException message) {
        return handleAsyncInternal(message);
    }
}
