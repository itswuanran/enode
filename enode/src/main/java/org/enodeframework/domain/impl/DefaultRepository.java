package org.enodeframework.domain.impl;

import org.enodeframework.common.utils.Assert;
import org.enodeframework.domain.AggregateRoot;
import org.enodeframework.domain.MemoryCache;
import org.enodeframework.domain.Repository;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class DefaultRepository implements Repository {

    private final MemoryCache memoryCache;

    public DefaultRepository(MemoryCache memoryCache) {
        this.memoryCache = memoryCache;
    }

    @Override
    public <T extends AggregateRoot> CompletableFuture<T> getAsync(Class<T> aggregateRootType, Object aggregateRootId) {
        Assert.nonNull(aggregateRootType, "aggregateRootType");
        Assert.nonNull(aggregateRootId, "aggregateRootId");
        CompletableFuture<T> future = memoryCache.getAsync(aggregateRootId, aggregateRootType);
        return future.thenCompose(aggregateRoot -> {
            if (aggregateRoot == null) {
                return memoryCache.refreshAggregateFromEventStoreAsync(aggregateRootType, aggregateRootId.toString());
            }
            return CompletableFuture.completedFuture(aggregateRoot);
        });
    }

    @Override
    public CompletableFuture<AggregateRoot> getAsync(Object aggregateRootId) {
        return getAsync(AggregateRoot.class, aggregateRootId);
    }

    @Override
    public <T extends AggregateRoot> void refreshAggregate(T aggregateRoot) {
        memoryCache.refreshAggregate(aggregateRoot);
    }
}
