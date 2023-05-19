package org.enodeframework.eventing

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class BatchAggregateEventAppendResult(private val expectedAggregateRootCount: Int) {
    private var aggregateEventAppendResultDict = ConcurrentHashMap<String, AggregateEventAppendResult>()
    var taskCompletionSource = CompletableFuture<EventAppendResult>()
    fun addCompleteAggregate(aggregateRootId: String, result: AggregateEventAppendResult) {
        if (aggregateEventAppendResultDict.putIfAbsent(aggregateRootId, result) == null) {
            val completedAggregateRootCount = aggregateEventAppendResultDict.keys.size
            if (completedAggregateRootCount == expectedAggregateRootCount) {
                val eventAppendResult = EventAppendResult()
                for ((key, value) in aggregateEventAppendResultDict) {
                    if (value.eventAppendStatus === EventAppendStatus.Success) {
                        eventAppendResult.addSuccessAggregateRootId(key)
                    } else if (value.eventAppendStatus === EventAppendStatus.DuplicateEvent) {
                        eventAppendResult.addDuplicateEventAggregateRootId(key)
                    } else if (value.eventAppendStatus === EventAppendStatus.DuplicateCommand) {
                        eventAppendResult.addDuplicateCommandIds(key, value.duplicateCommandIds)
                    }
                }
                taskCompletionSource.complete(eventAppendResult)
            }
        }
    }
}