package org.enodeframework.eventing

import com.google.common.collect.Maps
import org.enodeframework.messaging.AbstractMessage
import java.util.*

/**
 * Represents an abstract generic domain event.
 */
abstract class AbstractDomainEventMessage : AbstractMessage, DomainEventMessage {
    override var commandId: String = ""
    override var aggregateRootId: String = ""
    override var aggregateRootTypeName: String = ""
    final override var version: Int
    final override var sequence: Int
    override var timestamp: Date = Date()

    /**
     * Represents the extension key/values data of the message.
     */
    override var items: MutableMap<String, Any> = Maps.newHashMap()

    constructor() : super() {
        version = 1
        sequence = 1
    }

    constructor(id: String) : super(id) {
        version = 1
        sequence = 1
    }
}
