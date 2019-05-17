package com.enodeframework.samples.commandhandles.bank;

import com.enodeframework.annotation.Command;
import com.enodeframework.commanding.ICommandContext;
import com.enodeframework.commanding.ICommandHandler;
import com.enodeframework.samples.commands.bank.AddTransactionPreparationCommand;
import com.enodeframework.samples.domain.bank.bankaccount.BankAccount;

import java.util.concurrent.CompletableFuture;

/// <summary>银行账户相关命令处理
/// </summary>
@Command
public class AddTransactionPrepartionCommandHandler implements ICommandHandler<AddTransactionPreparationCommand>                   //开户
{
    @Override
    public CompletableFuture handleAsync(ICommandContext context, AddTransactionPreparationCommand command) {
        CompletableFuture<BankAccount> future = context.getAsync(command.getAggregateRootId(), BankAccount.class);
        future.thenAccept(account -> account.AddTransactionPreparation(command.TransactionId, command.TransactionType, command.PreparationType, command.Amount));
        return future;
    }
}
