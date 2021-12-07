package org.enodeframework.samples.domain.bank.deposittransaction;

import org.enodeframework.eventing.AbstractDomainEventMessage;

public class DepositTransactionStartedEvent extends AbstractDomainEventMessage<String> {
    public String accountId;
    public double amount;

    public DepositTransactionStartedEvent() {
    }

    public DepositTransactionStartedEvent(String accountId, double amount) {
        this.accountId = accountId;
        this.amount = amount;
    }
}
