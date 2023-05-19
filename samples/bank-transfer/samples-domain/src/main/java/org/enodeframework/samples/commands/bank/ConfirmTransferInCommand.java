package org.enodeframework.samples.commands.bank;

import org.enodeframework.commanding.AbstractCommandMessage;

/**
 * 确认转入
 */
public class ConfirmTransferInCommand extends AbstractCommandMessage {
    public ConfirmTransferInCommand() {
    }

    public ConfirmTransferInCommand(String transactionId) {
        super(transactionId);
    }
}
