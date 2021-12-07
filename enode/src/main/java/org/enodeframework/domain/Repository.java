package org.enodeframework.domain;

import java.util.concurrent.CompletableFuture;

public interface Repository {
    /**
     * Get an aggregate from memory cache, if not exist, get it from event store.
     */
    <T extends AggregateRoot> CompletableFuture<T> getAsync(Class<T> aggregateRootType, Object aggregateRootId);

    /**
     * Get an aggregate from memory cache, if not exist, get it from event store.
     */
    CompletableFuture<AggregateRoot> getAsync(Object aggregateRootId);

    /**
     * Refresh memory cache with given aggregate.
     */
    <T extends AggregateRoot> void refreshAggregate(T aggregateRoot);
}
