package org.enodeframework.samples.domain.bank.deposittransaction;

import org.enodeframework.eventing.AbstractDomainEventMessage;

/**
 * 存款交易已完成
 */
public class DepositTransactionCompletedEvent extends AbstractDomainEventMessage {
    public String accountId;

    public DepositTransactionCompletedEvent() {
    }

    public DepositTransactionCompletedEvent(String accountId) {
        this.accountId = accountId;
    }
}
