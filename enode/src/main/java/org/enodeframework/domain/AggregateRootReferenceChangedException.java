package org.enodeframework.domain;

public class AggregateRootReferenceChangedException extends RuntimeException {

    private final AggregateRoot aggregateRoot;

    public AggregateRootReferenceChangedException(AggregateRoot aggregateRoot) {
        super(String.format("Aggregate root [type=%s,id=%s] reference already changed.", aggregateRoot.getClass().getName(), aggregateRoot.getUniqueId()));
        this.aggregateRoot = aggregateRoot;
    }

    public AggregateRoot getAggregateRoot() {
        return aggregateRoot;
    }
}