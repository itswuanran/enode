package com.enodeframework.eventing;

import com.enodeframework.infrastructure.ISequenceMessage;

public interface IDomainEvent<TAggregateRootId> extends ISequenceMessage {
    TAggregateRootId aggregateRootId();

    void setAggregateRootId(TAggregateRootId aggregateRootId);
}
