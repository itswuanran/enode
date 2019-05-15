package com.enodeframework.samples.eventhandlers.bank.processmanagers;

import com.enodeframework.commanding.ICommandService;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.samples.applicationmessages.AccountValidateFailedMessage;
import com.enodeframework.samples.applicationmessages.AccountValidatePassedMessage;
import com.enodeframework.samples.commands.bank.AddTransactionPreparationCommand;
import com.enodeframework.samples.commands.bank.CancelTransferTransactionCommand;
import com.enodeframework.samples.commands.bank.CommitTransactionPreparationCommand;
import com.enodeframework.samples.commands.bank.ConfirmAccountValidatePassedCommand;
import com.enodeframework.samples.commands.bank.ConfirmTransferInCommand;
import com.enodeframework.samples.commands.bank.ConfirmTransferInPreparationCommand;
import com.enodeframework.samples.commands.bank.ConfirmTransferOutCommand;
import com.enodeframework.samples.commands.bank.ConfirmTransferOutPreparationCommand;
import com.enodeframework.samples.commands.bank.ValidateAccountCommand;
import com.enodeframework.samples.domain.bank.TransactionType;
import com.enodeframework.samples.domain.bank.bankaccount.InsufficientBalanceException;
import com.enodeframework.samples.domain.bank.bankaccount.PreparationType;
import com.enodeframework.samples.domain.bank.bankaccount.TransactionPreparationAddedEvent;
import com.enodeframework.samples.domain.bank.bankaccount.TransactionPreparationCommittedEvent;
import com.enodeframework.samples.domain.bank.transfertransaction.AccountValidatePassedConfirmCompletedEvent;
import com.enodeframework.samples.domain.bank.transfertransaction.TransferInPreparationConfirmedEvent;
import com.enodeframework.samples.domain.bank.transfertransaction.TransferOutPreparationConfirmedEvent;
import com.enodeframework.samples.domain.bank.transfertransaction.TransferTransactionStartedEvent;

import java.util.concurrent.CompletableFuture;

public abstract class AbstractTransferTransactionProcessManager {

    private ICommandService _commandService;

    public AbstractTransferTransactionProcessManager(ICommandService commandService) {
        _commandService = commandService;
    }

    public CompletableFuture<AsyncTaskResult> HandleAsync(TransferTransactionStartedEvent evnt) {
        ValidateAccountCommand command = new ValidateAccountCommand(evnt.TransactionInfo.SourceAccountId, evnt.aggregateRootId());
        command.setId(evnt.id());
        ValidateAccountCommand targetCommand = new ValidateAccountCommand(evnt.TransactionInfo.TargetAccountId, evnt.aggregateRootId());
        targetCommand.setId(evnt.id());

        CompletableFuture task1 = _commandService.sendAsync(command);
        CompletableFuture task2 = _commandService.sendAsync(targetCommand);
        CompletableFuture.allOf(task1, task2);
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }

    public CompletableFuture<AsyncTaskResult> HandleAsync(AccountValidatePassedMessage message) {
        ConfirmAccountValidatePassedCommand command = new ConfirmAccountValidatePassedCommand(message.TransactionId, message.AccountId);
        command.setId(message.id());
        return _commandService.sendAsync(command);
    }

    public CompletableFuture<AsyncTaskResult> HandleAsync(AccountValidateFailedMessage message) {
        CancelTransferTransactionCommand command = new CancelTransferTransactionCommand(message.TransactionId);
        command.setId(message.id());
        return _commandService.sendAsync(command);
    }

    public CompletableFuture<AsyncTaskResult> HandleAsync(AccountValidatePassedConfirmCompletedEvent evnt) {
        AddTransactionPreparationCommand command = new AddTransactionPreparationCommand(
                evnt.TransactionInfo.SourceAccountId,
                evnt.aggregateRootId(),
                TransactionType.TransferTransaction,
                PreparationType.DebitPreparation,
                evnt.TransactionInfo.Amount);
        command.setId(evnt.id());
        return _commandService.sendAsync(command);
    }

    public CompletableFuture<AsyncTaskResult> HandleAsync(TransactionPreparationAddedEvent evnt) {
        if (evnt.TransactionPreparation.transactionType == TransactionType.TransferTransaction) {
            if (evnt.TransactionPreparation.preparationType == PreparationType.DebitPreparation) {
                ConfirmTransferOutPreparationCommand command = new ConfirmTransferOutPreparationCommand(evnt.TransactionPreparation.TransactionId);
                command.setId(evnt.id());
                return _commandService.sendAsync(command);
            } else if (evnt.TransactionPreparation.preparationType == PreparationType.CreditPreparation) {
                ConfirmTransferInPreparationCommand command = new ConfirmTransferInPreparationCommand(evnt.TransactionPreparation.TransactionId);
                command.setId(evnt.id());
                return _commandService.sendAsync(command);
            }
        }
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }

    public CompletableFuture<AsyncTaskResult> HandleAsync(InsufficientBalanceException exception) {
        if (exception.TransactionType == TransactionType.TransferTransaction) {
            CancelTransferTransactionCommand command = new CancelTransferTransactionCommand(exception.TransactionId);
            command.setId(exception.id());
            return _commandService.sendAsync(command);
        }
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }

    public CompletableFuture<AsyncTaskResult> HandleAsync(TransferOutPreparationConfirmedEvent evnt) {
        AddTransactionPreparationCommand command = new AddTransactionPreparationCommand(
                evnt.TransactionInfo.TargetAccountId,
                evnt.aggregateRootId(),
                TransactionType.TransferTransaction,
                PreparationType.CreditPreparation,
                evnt.TransactionInfo.Amount);
        command.setId(evnt.id());
        return _commandService.sendAsync(command);
    }

    public CompletableFuture<AsyncTaskResult> HandleAsync(TransferInPreparationConfirmedEvent evnt) {
        CommitTransactionPreparationCommand command = new CommitTransactionPreparationCommand(evnt.TransactionInfo.SourceAccountId, evnt.aggregateRootId());
        command.setId(evnt.id());
        CommitTransactionPreparationCommand targetCommand = new CommitTransactionPreparationCommand(evnt.TransactionInfo.TargetAccountId, evnt.aggregateRootId());
        targetCommand.setId(evnt.id());
        CompletableFuture task1 = _commandService.sendAsync(command);
        CompletableFuture task2 = _commandService.sendAsync(targetCommand);
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }

    public CompletableFuture<AsyncTaskResult> HandleAsync(TransactionPreparationCommittedEvent evnt) {
        if (evnt.TransactionPreparation.transactionType == TransactionType.TransferTransaction) {
            if (evnt.TransactionPreparation.preparationType == PreparationType.DebitPreparation) {
                ConfirmTransferOutCommand command = new ConfirmTransferOutCommand(evnt.TransactionPreparation.TransactionId);
                command.setId(evnt.id());
                return _commandService.sendAsync(command);
            } else if (evnt.TransactionPreparation.preparationType == PreparationType.CreditPreparation) {
                ConfirmTransferInCommand command = new ConfirmTransferInCommand(evnt.TransactionPreparation.TransactionId);
                command.setId(evnt.id());
                return _commandService.sendAsync(command);
            }
        }
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }
}
