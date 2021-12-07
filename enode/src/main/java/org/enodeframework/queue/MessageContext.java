package org.enodeframework.queue;

public interface MessageContext {
    /**
     * 消息处理后执行
     */
    void onMessageHandled(QueueMessage message);
}
