package com.enodeframework.samples.eventhandlers.bank;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.infrastructure.IMessageHandler;
import com.enodeframework.samples.domain.bank.transfertransaction.TransferTransactionStartedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class TransferTransactionStartedEventHandler extends AbstractEventHandler implements IMessageHandler<TransferTransactionStartedEvent> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(TransferTransactionStartedEvent message) {
        return handleAsyncInternal(message);
    }
}
