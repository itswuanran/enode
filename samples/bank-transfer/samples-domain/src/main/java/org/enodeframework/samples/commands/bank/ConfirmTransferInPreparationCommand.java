package org.enodeframework.samples.commands.bank;

import org.enodeframework.commanding.AbstractCommandMessage;

/**
 * 确认预转入
 */
public class ConfirmTransferInPreparationCommand extends AbstractCommandMessage<String> {
    public ConfirmTransferInPreparationCommand() {
    }

    public ConfirmTransferInPreparationCommand(String transactionId) {
        super(transactionId);
    }
}
