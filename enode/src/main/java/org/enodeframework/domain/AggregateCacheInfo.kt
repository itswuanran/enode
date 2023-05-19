package org.enodeframework.domain

import org.enodeframework.common.extensions.SystemClock
import java.util.*

/**
 * @author anruence@gmail.com
 */
class AggregateCacheInfo(var aggregateRoot: AggregateRoot) {
    private var lastUpdateTime: Date

    init {
        lastUpdateTime = Date()
    }

    fun updateAggregateRoot(aggregateRoot: AggregateRoot) {
        this.aggregateRoot = aggregateRoot
        lastUpdateTime = Date()
    }

    fun isExpired(timeoutSeconds: Int): Boolean {
        return (SystemClock.now() - lastUpdateTime.time) / 1000 >= timeoutSeconds
    }
}
