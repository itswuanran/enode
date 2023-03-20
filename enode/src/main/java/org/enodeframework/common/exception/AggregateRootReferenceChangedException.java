package org.enodeframework.common.exception;

import org.enodeframework.domain.AggregateRoot;

public class AggregateRootReferenceChangedException extends EnodeException {

    private final AggregateRoot aggregateRoot;

    public AggregateRootReferenceChangedException(AggregateRoot aggregateRoot) {
        super(String.format("Aggregate root [type=%s,id=%s] reference already changed.", aggregateRoot.getClass().getName(), aggregateRoot.getUniqueId()));
        this.aggregateRoot = aggregateRoot;
    }

    public AggregateRoot getAggregateRoot() {
        return aggregateRoot;
    }
}