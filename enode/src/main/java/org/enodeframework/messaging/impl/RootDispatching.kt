package org.enodeframework.messaging.impl

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class RootDispatching {
    val taskCompletionSource: CompletableFuture<Boolean> = CompletableFuture()
    private val childDispatchingDict: ConcurrentHashMap<Any, Boolean> = ConcurrentHashMap()

    fun addChildDispatching(childDispatching: Any) {
        childDispatchingDict[childDispatching] = false
    }

    fun onChildDispatchingFinished(childDispatching: Any) {
        if (childDispatchingDict.remove(childDispatching) != null) {
            if (childDispatchingDict.isEmpty()) {
                taskCompletionSource.complete(true)
            }
        }
    }
}
