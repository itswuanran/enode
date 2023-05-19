package org.enodeframework.eventing

import com.google.common.collect.Lists

class AggregateEventAppendResult(var eventAppendStatus: EventAppendStatus) {
    var duplicateCommandIds: List<String> = Lists.newArrayList()
}
