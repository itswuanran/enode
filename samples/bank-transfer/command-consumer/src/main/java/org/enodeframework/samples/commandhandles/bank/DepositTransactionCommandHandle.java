package org.enodeframework.samples.commandhandles.bank;

import org.enodeframework.annotation.Command;
import org.enodeframework.annotation.Subscribe;
import org.enodeframework.commanding.CommandContext;
import org.enodeframework.samples.commands.bank.ConfirmDepositCommand;
import org.enodeframework.samples.commands.bank.ConfirmDepositPreparationCommand;
import org.enodeframework.samples.commands.bank.StartDepositTransactionCommand;
import org.enodeframework.samples.domain.bank.deposittransaction.DepositTransaction;

import java.util.concurrent.CompletableFuture;

/**
 * 银行存款交易相关命令处理
 * <StartDepositTransactionCommand>,                      //开始交易
 * <ConfirmDepositPreparationCommand>,                    //确认预存款
 * <ConfirmDepositCommand>                                //确认存款
 */
@Command
public class DepositTransactionCommandHandle {
    @Subscribe
    public void handleAsync(CommandContext context, StartDepositTransactionCommand command) {
        context.addAsync(new DepositTransaction(command.getAggregateRootId(), command.accountId, command.amount));
    }

    @Subscribe
    public CompletableFuture<DepositTransaction> handleAsync(CommandContext context, ConfirmDepositPreparationCommand command) {
        CompletableFuture<DepositTransaction> future = context.getAsync(command.getAggregateRootId(), DepositTransaction.class);
        future.thenAccept(DepositTransaction::confirmDepositPreparation);
        return future;
    }

    @Subscribe
    public CompletableFuture<DepositTransaction> handleAsync(CommandContext context, ConfirmDepositCommand command) {
        CompletableFuture<DepositTransaction> future = context.getAsync(command.getAggregateRootId(), DepositTransaction.class);
        future.thenAccept(DepositTransaction::confirmDeposit);
        return future;
    }
}