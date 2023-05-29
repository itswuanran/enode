package org.enodeframework.queue.domainevent

import com.google.common.collect.Maps
import java.io.Serializable
import java.util.*

/**
 * @author anruence@gmail.com
 */
class GenericDomainEventMessage : Serializable {
    var id: String = ""
    var aggregateRootId: String = ""
    var aggregateRootTypeName: String = ""
    var version = 0
    var timestamp: Date = Date()
    var commandId: String = ""
    var events: Map<String, String> = Maps.newHashMap()
    var items: MutableMap<String, Any> = Maps.newHashMap()
}
