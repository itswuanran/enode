package org.enodeframework.eventing

import com.google.common.collect.Lists
import com.google.common.collect.Maps

class EventAppendResult {
    private val lockObj = Any()
    var successAggregateRootIdList: MutableList<String> = Lists.newArrayList()
    var duplicateEventAggregateRootIdList: MutableList<String> = Lists.newArrayList()
    var duplicateCommandAggregateRootIdList: MutableMap<String, List<String>> = Maps.newHashMap()

    fun addSuccessAggregateRootId(aggregateRootId: String) {
        synchronized(lockObj) {
            if (!successAggregateRootIdList.contains(aggregateRootId)) {
                successAggregateRootIdList.add(aggregateRootId)
            }
        }
    }

    fun addDuplicateEventAggregateRootId(aggregateRootId: String) {
        synchronized(lockObj) {
            if (!duplicateEventAggregateRootIdList.contains(aggregateRootId)) {
                duplicateEventAggregateRootIdList.add(aggregateRootId)
            }
        }
    }

    fun addDuplicateCommandIds(aggregateRootId: String, aggregateDuplicateCommandIdList: List<String>) {
        synchronized(lockObj) {
            if (!duplicateCommandAggregateRootIdList.containsKey(aggregateRootId)) {
                duplicateCommandAggregateRootIdList[aggregateRootId] = aggregateDuplicateCommandIdList
            }
        }
    }
}
