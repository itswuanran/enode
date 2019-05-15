package com.enodeframework.samples.commandhandles.bank;

import com.enodeframework.commanding.ICommandContext;
import com.enodeframework.commanding.ICommandHandler;
import com.enodeframework.samples.commands.bank.ConfirmTransferOutCommand;
import com.enodeframework.samples.domain.bank.transfertransaction.TransferTransaction;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class ConfirmTransferOutCommandHandle implements ICommandHandler<ConfirmTransferOutCommand> {
    @Override
    public CompletableFuture handleAsync(ICommandContext context, ConfirmTransferOutCommand command) {
        CompletableFuture<TransferTransaction> future = context.getAsync(command.getAggregateRootId(), TransferTransaction.class);
        future.thenAccept(transaction -> transaction.ConfirmTransferOut());
        return future;
    }
}