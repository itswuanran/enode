package org.enodeframework.samples.commands.bank;

import org.enodeframework.commanding.AbstractCommandMessage;

/**
 * 确认存款
 */
public class ConfirmDepositCommand extends AbstractCommandMessage<String> {
    public ConfirmDepositCommand() {
    }

    public ConfirmDepositCommand(String transactionId) {
        super(transactionId);
    }
}
