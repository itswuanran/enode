package org.enodeframework.samples.commands.bank;

import org.enodeframework.commanding.AbstractCommandMessage;

/**
 * 确认预存款
 */
public class ConfirmDepositPreparationCommand extends AbstractCommandMessage<String> {
    public ConfirmDepositPreparationCommand() {
    }

    public ConfirmDepositPreparationCommand(String transactionId) {
        super(transactionId);
    }
}
