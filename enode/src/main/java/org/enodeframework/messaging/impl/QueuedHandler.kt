package org.enodeframework.messaging.impl

import org.enodeframework.common.function.Action2
import org.enodeframework.infrastructure.ObjectProxy
import java.util.concurrent.ConcurrentLinkedQueue

class QueuedHandler<T : ObjectProxy?>(handlers: List<T>, dispatchToNextHandler: Action2<QueuedHandler<T>, T>) {
    private val dispatchToNextHandler: Action2<QueuedHandler<T>, T>
    private val handlerQueue: ConcurrentLinkedQueue<T> = ConcurrentLinkedQueue()

    init {
        handlerQueue.addAll(handlers)
        this.dispatchToNextHandler = dispatchToNextHandler
    }

    fun dequeueHandler(): T {
        return handlerQueue.poll()
    }

    fun onHandlerFinished(handler: T) {
        val nextHandler = dequeueHandler()
        if (nextHandler != null) {
            dispatchToNextHandler.apply(this, nextHandler)
        }
    }
}
