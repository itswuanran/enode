package org.enodeframework.samples.commands.bank;

import org.enodeframework.commanding.AbstractCommandMessage;

/**
 * 取消转账交易
 */
public class CancelTransferTransactionCommand extends AbstractCommandMessage<String> {
    public CancelTransferTransactionCommand() {
    }

    public CancelTransferTransactionCommand(String transactionId) {
        super(transactionId);
    }
}
