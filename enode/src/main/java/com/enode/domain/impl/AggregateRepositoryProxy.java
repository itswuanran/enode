package com.enode.domain.impl;

import com.enode.domain.IAggregateRepository;
import com.enode.domain.IAggregateRepositoryProxy;
import com.enode.domain.IAggregateRoot;

import java.util.concurrent.CompletableFuture;

public class AggregateRepositoryProxy<TAggregateRoot extends IAggregateRoot> implements IAggregateRepositoryProxy {

    private final IAggregateRepository<TAggregateRoot> aggregateRepository;

    public AggregateRepositoryProxy(IAggregateRepository<TAggregateRoot> aggregateRepository) {
        this.aggregateRepository = aggregateRepository;
    }

    @Override
    public Object getInnerObject() {
        return aggregateRepository;
    }

    @Override
    public CompletableFuture<IAggregateRoot> getAsync(String aggregateRootId) {
        return (CompletableFuture<IAggregateRoot>) aggregateRepository.getAsync(aggregateRootId);
    }
}
