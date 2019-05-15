package com.enodeframework.samples.commandhandles.bank;

import com.enodeframework.commanding.ICommandContext;
import com.enodeframework.commanding.ICommandHandler;
import com.enodeframework.samples.commands.bank.ConfirmDepositPreparationCommand;
import com.enodeframework.samples.domain.bank.deposittransaction.DepositTransaction;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * 确认预存款
 */
@Component
public class ConfirmDepositPreparationCommandHandler implements ICommandHandler<ConfirmDepositPreparationCommand> {
    @Override
    public CompletableFuture handleAsync(ICommandContext context, ConfirmDepositPreparationCommand command) {
        CompletableFuture<DepositTransaction> future = context.getAsync(command.getAggregateRootId(), DepositTransaction.class);
        future.thenAccept(DepositTransaction::ConfirmDepositPreparation);
        return future;
    }
}