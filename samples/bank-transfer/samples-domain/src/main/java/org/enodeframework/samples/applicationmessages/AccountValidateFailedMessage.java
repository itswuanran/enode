package org.enodeframework.samples.applicationmessages;

import org.enodeframework.messaging.AbstractApplicationMessage;

/**
 * 账户验证未通过
 */
public class AccountValidateFailedMessage extends AbstractApplicationMessage {
    public String accountId;
    public String transactionId;
    public String reason;

    public AccountValidateFailedMessage() {
    }

    public AccountValidateFailedMessage(String accountId, String transactionId, String reason) {
        this.accountId = accountId;
        this.transactionId = transactionId;
        this.reason = reason;
    }
}
