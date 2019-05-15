package com.enodeframework.samples.eventhandlers.bank;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.infrastructure.IMessageHandler;
import com.enodeframework.samples.domain.bank.transfertransaction.TransferInPreparationConfirmedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class TransferInPreparationConfirmedEventHandler extends AbstractEventHandler implements IMessageHandler<TransferInPreparationConfirmedEvent> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(TransferInPreparationConfirmedEvent message) {
        return handleAsyncInternal(message);
    }
}
