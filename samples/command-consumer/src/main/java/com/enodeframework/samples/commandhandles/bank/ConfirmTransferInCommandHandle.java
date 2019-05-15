package com.enodeframework.samples.commandhandles.bank;

import com.enodeframework.commanding.ICommandContext;
import com.enodeframework.commanding.ICommandHandler;
import com.enodeframework.samples.commands.bank.ConfirmTransferInCommand;
import com.enodeframework.samples.domain.bank.transfertransaction.TransferTransaction;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class ConfirmTransferInCommandHandle implements ICommandHandler<ConfirmTransferInCommand> {
    @Override
    public CompletableFuture handleAsync(ICommandContext context, ConfirmTransferInCommand command) {
        CompletableFuture<TransferTransaction> future = context.getAsync(command.getAggregateRootId(), TransferTransaction.class);
        future.thenAccept(transaction -> transaction.ConfirmTransferIn());
        return future;
    }
}