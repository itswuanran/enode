package org.enodeframework.samples.commands.bank;

import org.enodeframework.commanding.AbstractCommandMessage;

/**
 * 确认账户验证已通过
 */
public class ConfirmAccountValidatePassedCommand extends AbstractCommandMessage {
    /**
     * 账户ID
     */
    public String accountId;

    public ConfirmAccountValidatePassedCommand() {
    }

    public ConfirmAccountValidatePassedCommand(String transactionId, String accountId) {
        super(transactionId);
        this.accountId = accountId;
    }
}
