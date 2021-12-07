package org.enodeframework.samples.domain.bank.bankaccount;

import org.enodeframework.eventing.AbstractDomainEventMessage;

/**
 * 账户预操作已添加
 */
public class TransactionPreparationAddedEvent extends AbstractDomainEventMessage<String> {
    public TransactionPreparation transactionPreparation;

    public TransactionPreparationAddedEvent() {
    }

    public TransactionPreparationAddedEvent(TransactionPreparation transactionPreparation) {
        this.transactionPreparation = transactionPreparation;
    }
}
