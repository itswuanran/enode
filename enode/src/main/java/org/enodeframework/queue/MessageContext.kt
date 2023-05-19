package org.enodeframework.queue

interface MessageContext {
    /**
     * 消息处理后执行
     */
    fun onMessageHandled(message: QueueMessage)
}
