package org.enodeframework.samples.applicationmessages;

import org.enodeframework.messaging.AbstractApplicationMessage;

/**
 * 账户验证已通过
 */
public class AccountValidatePassedMessage extends AbstractApplicationMessage {
    public String accountId;
    public String transactionId;

    public AccountValidatePassedMessage() {
    }

    public AccountValidatePassedMessage(String accountId, String transactionId) {
        this.accountId = accountId;
        this.transactionId = transactionId;
    }
}
