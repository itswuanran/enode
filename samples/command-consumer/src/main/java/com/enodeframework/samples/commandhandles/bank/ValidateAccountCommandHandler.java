package com.enodeframework.samples.commandhandles.bank;

import com.enodeframework.annotation.Command;
import com.enodeframework.commanding.ICommandAsyncHandler;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.io.AsyncTaskStatus;
import com.enodeframework.infrastructure.IApplicationMessage;
import com.enodeframework.samples.applicationmessages.AccountValidateFailedMessage;
import com.enodeframework.samples.applicationmessages.AccountValidatePassedMessage;
import com.enodeframework.samples.commands.bank.ValidateAccountCommand;

import java.util.concurrent.CompletableFuture;

/// <summary>银行账户相关命令处理
/// </summary>
//验证账户是否合法
//开户
@Command
public class ValidateAccountCommandHandler implements ICommandAsyncHandler<ValidateAccountCommand> {
    @Override
    public CompletableFuture<AsyncTaskResult<IApplicationMessage>> handleAsync(ValidateAccountCommand command) {
        IApplicationMessage applicationMessage;

        //此处应该会调用外部接口验证账号是否合法，这里仅仅简单通过账号是否以INVALID字符串开头来判断是否合法；根据账号的合法性，返回不同的应用层消息
        if (command.getAggregateRootId().startsWith("INVALID")) {
            applicationMessage = new AccountValidateFailedMessage(command.getAggregateRootId(), command.TransactionId, "账户不合法.");
        } else {
            applicationMessage = new AccountValidatePassedMessage(command.getAggregateRootId(), command.TransactionId);
        }

        return CompletableFuture.completedFuture(new AsyncTaskResult<>(AsyncTaskStatus.Success, applicationMessage));
    }
}
