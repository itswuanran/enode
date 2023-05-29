package org.enodeframework.queue

import com.google.common.base.MoreObjects
import java.io.Serializable

/**
 * @author anruence@gmail.com
 */
class QueueMessage : Serializable {
    /**
     * 消息体
     */
    var body: String = ""

    /**
     * topic
     */
    var topic: String = ""

    /**
     * 业务标识
     */
    var tag: String = ""

    /**
     * 路由的键
     */
    var routeKey: String = ""

    /**
     * 消息唯一标识
     */
    var key: String = ""

    /**
     * 消息类型
     * [MessageTypeCode]
     */
    var type: Char = MessageTypeCode.Default.value
    val bodyAndType: String
        get() = "$body|$type"

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("body", body)
            .add("topic", topic)
            .add("tag", tag)
            .add("routeKey", routeKey)
            .add("key", key)
            .add("type", type)
            .toString()
    }
}
