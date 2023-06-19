package org.enodeframework.queue

import com.google.common.collect.Maps
import java.io.Serializable

/**
 * @author anruence@gmail.com
 */
class SendMessageResult(var id: String, var items: MutableMap<String, Any>) : Serializable {
    constructor(id: String) : this(id, Maps.newHashMap())
}