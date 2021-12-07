package org.enodeframework.samples.domain.bank.transfertransaction;

import org.enodeframework.eventing.AbstractDomainEventMessage;

/**
 * 转账交易已开始
 */
public abstract class AbstractTransferTransactionEvent extends AbstractDomainEventMessage<String> {
    public TransferTransactionInfo transferTransactionInfo;

    public AbstractTransferTransactionEvent() {
    }

    public AbstractTransferTransactionEvent(TransferTransactionInfo transferTransactionInfo) {
        this.transferTransactionInfo = transferTransactionInfo;
    }
}