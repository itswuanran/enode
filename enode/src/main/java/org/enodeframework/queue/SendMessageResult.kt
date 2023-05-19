package org.enodeframework.queue

import com.google.common.collect.Maps
import java.io.Serializable

/**
 * @author anruence@gmail.com
 */
class SendMessageResult(var id: String, result: Any) : Serializable {

    var items: MutableMap<String, Any> = Maps.newHashMap()

    init {
        this.items["result"] = result
    }
}