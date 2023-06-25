package org.enodeframework.messaging.impl

import org.enodeframework.messaging.Message
import java.util.concurrent.ConcurrentLinkedQueue

class QueueMessageDispatching(
    private val dispatcher: DefaultMessageDispatcher,
    rootDispatching: RootDispatching,
    messages: List<Message>
) {
    private val rootDispatching: RootDispatching
    private val messageQueue: ConcurrentLinkedQueue<Message> = ConcurrentLinkedQueue()

    init {
        messageQueue.addAll(messages)
        this.rootDispatching = rootDispatching
        this.rootDispatching.addChildDispatching(this)
    }

    fun dequeueMessage(): Message? {
        return messageQueue.poll()
    }

    fun onMessageHandled() {
        val nextMessage = dequeueMessage()
        if (nextMessage == null) {
            rootDispatching.onChildDispatchingFinished(this)
            return
        }
        dispatcher.dispatchSingleMessage(nextMessage, this)
    }
}
