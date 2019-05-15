package com.enodeframework.samples.eventhandlers.bank;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.infrastructure.IMessageHandler;
import com.enodeframework.samples.domain.bank.bankaccount.TransactionPreparationAddedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class TransactionPreparationAddedEventHandler extends AbstractEventHandler implements IMessageHandler<TransactionPreparationAddedEvent> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(TransactionPreparationAddedEvent message) {
        return handleAsyncInternal(message);
    }
}
