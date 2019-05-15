package com.enodeframework.samples.eventhandlers.bank;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.infrastructure.IMessageHandler;
import com.enodeframework.samples.domain.bank.transfertransaction.TransferTransactionCanceledEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class TransferTransactionCanceledEventHandler extends AbstractEventHandler implements IMessageHandler<TransferTransactionCanceledEvent> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(TransferTransactionCanceledEvent message) {
        return handleAsyncInternal(message);
    }
}
