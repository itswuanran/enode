package org.enodeframework.domain.impl;

import org.enodeframework.common.io.IOHelper;
import org.enodeframework.domain.AggregateRepositoryProvider;
import org.enodeframework.domain.AggregateRepositoryProxy;
import org.enodeframework.domain.AggregateRoot;
import org.enodeframework.domain.AggregateSnapshotter;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class DefaultAggregateSnapshotter implements AggregateSnapshotter {

    private final AggregateRepositoryProvider aggregateRepositoryProvider;

    public DefaultAggregateSnapshotter(AggregateRepositoryProvider aggregateRepositoryProvider) {
        this.aggregateRepositoryProvider = aggregateRepositoryProvider;
    }

    @Override
    public <T extends AggregateRoot> CompletableFuture<T> restoreFromSnapshotAsync(Class<T> aggregateRootType, String aggregateRootId) {
        AggregateRepositoryProxy aggregateRepository = aggregateRepositoryProvider.getRepository(aggregateRootType);
        return tryGetAggregateAsync(aggregateRepository, aggregateRootType, aggregateRootId, 0);
    }

    private <T extends AggregateRoot> CompletableFuture<T> tryGetAggregateAsync(AggregateRepositoryProxy aggregateRepository, Class<?> aggregateRootType, String aggregateRootId, int retryTimes) {
        CompletableFuture<T> taskSource = new CompletableFuture<>();
        if (aggregateRepository == null) {
            taskSource.complete(null);
            return taskSource;
        }
        IOHelper.tryAsyncActionRecursively("TryGetAggregateAsync",
            () -> aggregateRepository.getAsync(aggregateRootId),
            result -> {
                taskSource.complete((T) result);
            },
            () -> String.format("aggregateRepository.getAsync has unknown exception, aggregateRepository: %s, aggregateRootTypeName: %s, aggregateRootId: %s", aggregateRepository.getClass().getName(), aggregateRootType.getName(), aggregateRootId),
            null,
            retryTimes,
            true);
        return taskSource;
    }
}
