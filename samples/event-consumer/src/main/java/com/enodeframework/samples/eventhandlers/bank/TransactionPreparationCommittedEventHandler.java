package com.enodeframework.samples.eventhandlers.bank;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.infrastructure.IMessageHandler;
import com.enodeframework.samples.domain.bank.bankaccount.TransactionPreparationCommittedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class TransactionPreparationCommittedEventHandler extends AbstractEventHandler implements IMessageHandler<TransactionPreparationCommittedEvent> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(TransactionPreparationCommittedEvent message) {
        return handleAsyncInternal(message);
    }
}
