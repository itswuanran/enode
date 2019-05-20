package com.enodeframework.samples.commandhandles.bank;

import com.enodeframework.annotation.Command;
import com.enodeframework.commanding.ICommandContext;
import com.enodeframework.common.io.Await;
import com.enodeframework.samples.commands.bank.ConfirmDepositCommand;
import com.enodeframework.samples.commands.bank.ConfirmDepositPreparationCommand;
import com.enodeframework.samples.commands.bank.StartDepositTransactionCommand;
import com.enodeframework.samples.domain.bank.deposittransaction.DepositTransaction;

import java.util.concurrent.CompletableFuture;

/**
 * 银行存款交易相关命令处理
 */
@Command
public class DepositTransactionCommandHandle {

    public void handleAsync(ICommandContext context, StartDepositTransactionCommand command) {
        context.addAsync(new DepositTransaction(command.getAggregateRootId(), command.AccountId, command.Amount));
    }

    public void handleAsync(ICommandContext context, ConfirmDepositPreparationCommand command) {
        CompletableFuture<DepositTransaction> future = context.getAsync(command.getAggregateRootId(), DepositTransaction.class);
        DepositTransaction depositTransaction = Await.get(future);
        depositTransaction.ConfirmDepositPreparation();
    }

    public void handleAsync(ICommandContext context, ConfirmDepositCommand command) {
        CompletableFuture<DepositTransaction> future = context.getAsync(command.getAggregateRootId(), DepositTransaction.class);
        DepositTransaction depositTransaction = Await.get(future);
        depositTransaction.ConfirmDeposit();
    }
}