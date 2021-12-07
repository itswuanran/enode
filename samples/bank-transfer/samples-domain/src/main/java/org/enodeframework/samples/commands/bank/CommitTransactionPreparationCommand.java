package org.enodeframework.samples.commands.bank;

import org.enodeframework.commanding.AbstractCommandMessage;

/**
 * 提交预操作
 */
public class CommitTransactionPreparationCommand extends AbstractCommandMessage<String> {
    public String transactionId;

    public CommitTransactionPreparationCommand() {
    }

    public CommitTransactionPreparationCommand(String accountId, String transactionId) {
        super(accountId);
        this.transactionId = transactionId;
    }
}
