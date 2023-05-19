package org.enodeframework.samples.domain.bank.deposittransaction;

import org.enodeframework.eventing.AbstractDomainEventMessage;

/**
 * 存款交易预存款已确认
 */
public class DepositTransactionPreparationCompletedEvent extends AbstractDomainEventMessage {
    public String accountId;

    public DepositTransactionPreparationCompletedEvent() {
    }

    public DepositTransactionPreparationCompletedEvent(String accountId) {
        this.accountId = accountId;
    }
}
