package org.enodeframework.queue.publishableexceptions

import com.google.common.collect.Maps
import java.io.Serializable
import java.util.*

/**
 * @author anruence@gmail.com
 */
class GenericPublishableExceptionMessage : Serializable {
    var uniqueId: String = ""

    var exceptionType: String = ""

    var timestamp: Date = Date()

    var serializableInfo: MutableMap<String, Any> = Maps.newHashMap()

    var items: MutableMap<String, Any> = Maps.newHashMap()
}
