package com.enodeframework.samples.eventhandlers.bank;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.infrastructure.IMessageHandler;
import com.enodeframework.samples.domain.bank.transfertransaction.TransferOutPreparationConfirmedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class TransferOutPreparationConfirmedEventHandler extends AbstractEventHandler implements IMessageHandler<TransferOutPreparationConfirmedEvent> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(TransferOutPreparationConfirmedEvent message) {
        return handleAsyncInternal(message);
    }
}
