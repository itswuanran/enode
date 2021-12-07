package org.enodeframework.domain;

import java.util.concurrent.CompletableFuture;

/**
 * An interface which can restore aggregate from snapshot storage.
 */
public interface AggregateSnapshotter {
    /**
     * Restore the aggregate from snapshot storage.
     */
    <T extends AggregateRoot> CompletableFuture<T> restoreFromSnapshotAsync(Class<T> aggregateRootType, String aggregateRootId);
}
