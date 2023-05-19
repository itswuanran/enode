package org.enodeframework.samples.commands.bank;

import org.enodeframework.commanding.AbstractCommandMessage;

/**
 * 确认预转出
 */
public class ConfirmTransferOutPreparationCommand extends AbstractCommandMessage {
    public ConfirmTransferOutPreparationCommand() {
    }

    public ConfirmTransferOutPreparationCommand(String transactionId) {
        super(transactionId);
    }
}
