package org.enodeframework.samples.commandhandles.bank;

import org.enodeframework.annotation.Command;
import org.enodeframework.annotation.Subscribe;
import org.enodeframework.commanding.CommandContext;
import org.enodeframework.messaging.ApplicationMessage;
import org.enodeframework.samples.applicationmessages.AccountValidateFailedMessage;
import org.enodeframework.samples.applicationmessages.AccountValidatePassedMessage;
import org.enodeframework.samples.commands.bank.AddTransactionPreparationCommand;
import org.enodeframework.samples.commands.bank.CommitTransactionPreparationCommand;
import org.enodeframework.samples.commands.bank.CreateAccountCommand;
import org.enodeframework.samples.commands.bank.ValidateAccountCommand;
import org.enodeframework.samples.domain.bank.bankaccount.BankAccount;

import java.util.concurrent.CompletableFuture;

/**
 * 银行账户相关命令处理
 * ICommandHandler<CreateAccountCommand>,                       //开户
 * ICommandAsyncHandler<ValidateAccountCommand>,                //验证账户是否合法
 * ICommandHandler<AddTransactionPreparationCommand>,           //添加预操作
 * ICommandHandler<CommitTransactionPreparationCommand>         //提交预操作
 */
@Command
public class BankAccountCommandHandler {
    /**
     * 开户
     */
    @Subscribe
    public void handleAsync(CommandContext context, CreateAccountCommand command) {
        context.addAsync(new BankAccount(command.getAggregateRootId(), command.owner));
    }

    /**
     * 添加预操作
     */
    @Subscribe
    public CompletableFuture<BankAccount> handleAsync(CommandContext context, AddTransactionPreparationCommand command) {
        CompletableFuture<BankAccount> future = context.getAsync(command.getAggregateRootId(), BankAccount.class);
        future.thenAccept(bankAccount -> {
            bankAccount.addTransactionPreparation(command.transactionId, command.transactionType, command.preparationType, command.amount);
        });
        return future;
    }

    /**
     * 验证账户是否合法
     */
    @Subscribe
    public void handleAsync(CommandContext context, ValidateAccountCommand command) {
        ApplicationMessage applicationMessage = new AccountValidatePassedMessage(command.getAggregateRootId(), command.transactionId);
        //此处应该会调用外部接口验证账号是否合法，这里仅仅简单通过账号是否以INVALID字符串开头来判断是否合法；根据账号的合法性，返回不同的应用层消息
        if (command.getAggregateRootId().startsWith("INVALID")) {
            applicationMessage = new AccountValidateFailedMessage(command.getAggregateRootId(), command.transactionId, "账户不合法.");
        }
        context.setApplicationMessage(applicationMessage);
    }

    /**
     * 提交预操作
     */
    @Subscribe
    public CompletableFuture<BankAccount> handleAsync(CommandContext context, CommitTransactionPreparationCommand command) {
        CompletableFuture<BankAccount> future = context.getAsync(command.getAggregateRootId(), BankAccount.class);
        future.thenAccept(bankAccount -> {
            bankAccount.commitTransactionPreparation(command.transactionId);
        });
        return future;
    }
}
