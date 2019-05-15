package com.enodeframework.samples.commandhandles.bank;

import com.enodeframework.commanding.ICommandContext;
import com.enodeframework.commanding.ICommandHandler;
import com.enodeframework.samples.commands.bank.StartTransferTransactionCommand;
import com.enodeframework.samples.domain.bank.transfertransaction.TransferTransaction;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * 开始转账交易
 */
@Component
public class StartTransferTransactionCommandHandle implements ICommandHandler<StartTransferTransactionCommand> {
    @Override
    public CompletableFuture handleAsync(ICommandContext context, StartTransferTransactionCommand command) {
        return context.addAsync(new TransferTransaction(command.getAggregateRootId(), command.TransactionInfo));
    }
}