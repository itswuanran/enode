package org.enodeframework.queue

import com.google.common.base.MoreObjects
import com.google.common.collect.Maps
import java.io.Serializable

/**
 * @author anruence@gmail.com
 */
class QueueMessage : Serializable {
    /**
     * 消息体
     */
    var body: ByteArray = ByteArray(0)

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
    var type: String = MessageTypeCode.Default.value

    /**
     * 扩展信息
     */
    var items: MutableMap<String, String> = Maps.newHashMap()

    fun bodyAsStr(): String {
        return body.decodeToString()
    }

    fun channel(): String {
        return "$topic#$tag"
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this).add("body", bodyAsStr()).add("topic", topic).add("tag", tag)
            .add("routeKey", routeKey).add("key", key).add("type", type).toString()
    }
}
