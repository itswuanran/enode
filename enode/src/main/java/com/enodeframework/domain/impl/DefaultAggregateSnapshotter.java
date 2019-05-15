package com.enodeframework.domain.impl;

import com.enodeframework.domain.IAggregateRepositoryProvider;
import com.enodeframework.domain.IAggregateRepositoryProxy;
import com.enodeframework.domain.IAggregateRoot;
import com.enodeframework.domain.IAggregateSnapshotter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

public class DefaultAggregateSnapshotter implements IAggregateSnapshotter {

    @Autowired
    private IAggregateRepositoryProvider aggregateRepositoryProvider;

    @Override
    public CompletableFuture<IAggregateRoot> restoreFromSnapshotAsync(Class aggregateRootType, String aggregateRootId) {
        IAggregateRepositoryProxy aggregateRepository = aggregateRepositoryProvider.getRepository(aggregateRootType);
        if (aggregateRepository == null) {
            return CompletableFuture.completedFuture(null);
        }
        return aggregateRepository.getAsync(aggregateRootId);
    }
}
