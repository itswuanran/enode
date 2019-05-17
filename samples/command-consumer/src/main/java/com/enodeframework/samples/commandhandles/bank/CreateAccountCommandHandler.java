package com.enodeframework.samples.commandhandles.bank;

import com.enodeframework.annotation.Command;
import com.enodeframework.commanding.ICommandContext;
import com.enodeframework.commanding.ICommandHandler;
import com.enodeframework.samples.commands.bank.CreateAccountCommand;
import com.enodeframework.samples.domain.bank.bankaccount.BankAccount;

import java.util.concurrent.CompletableFuture;

/// <summary>银行账户相关命令处理
/// </summary>
@Command
public class CreateAccountCommandHandler implements ICommandHandler<CreateAccountCommand>                   //开户
{
    @Override
    public CompletableFuture handleAsync(ICommandContext context, CreateAccountCommand command) {
        return context.addAsync(new BankAccount(command.getAggregateRootId(), command.Owner));
    }
}
