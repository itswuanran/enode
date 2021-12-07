package org.enodeframework.samples.domain.bank.bankaccount;

import org.enodeframework.eventing.AbstractDomainEventMessage;

/**
 * 账户预操作已取消
 */
public class TransactionPreparationCanceledEvent extends AbstractDomainEventMessage<String> {
    public TransactionPreparation transactionPreparation;

    public TransactionPreparationCanceledEvent() {
    }

    public TransactionPreparationCanceledEvent(TransactionPreparation transactionPreparation) {
        this.transactionPreparation = transactionPreparation;
    }
}
