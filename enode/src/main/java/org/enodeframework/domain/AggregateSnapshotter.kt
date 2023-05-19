package org.enodeframework.domain

import java.util.concurrent.CompletableFuture

/**
 * An interface which can restore aggregate from snapshot storage.
 */
interface AggregateSnapshotter {
    /**
     * Restore the aggregate from snapshot storage.
     */
    fun <T : AggregateRoot?> restoreFromSnapshotAsync(
        aggregateRootType: Class<T>,
        aggregateRootId: String
    ): CompletableFuture<T>
}
