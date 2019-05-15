package com.enodeframework.samples.commandhandles.bank;

import com.enodeframework.commanding.ICommandContext;
import com.enodeframework.commanding.ICommandHandler;
import com.enodeframework.samples.commands.bank.StartDepositTransactionCommand;
import com.enodeframework.samples.domain.bank.deposittransaction.DepositTransaction;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * 开始交易
 */
@Component
public class StartDepositTransactionCommandHandler implements ICommandHandler<StartDepositTransactionCommand> {

    @Override
    public CompletableFuture handleAsync(ICommandContext context, StartDepositTransactionCommand command) {
        return context.addAsync(new DepositTransaction(command.getAggregateRootId(), command.AccountId, command.Amount));
    }
}