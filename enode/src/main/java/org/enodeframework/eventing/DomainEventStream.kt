package org.enodeframework.eventing

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import org.enodeframework.common.exception.DomainEventInvalidException
import org.enodeframework.messaging.AbstractMessage
import java.util.*

/**
 * @author anruence@gmail.com
 */
class DomainEventStream : AbstractMessage {
    var commandId: String = ""

    var aggregateRootTypeName: String = ""

    var aggregateRootId: String = ""

    var version = 0

    var events: List<DomainEventMessage> = Lists.newArrayList()

    override var items: MutableMap<String, Any> = Maps.newHashMap()

    override var timestamp: Date = Date()

    constructor()
    constructor(
        commandId: String,
        aggregateRootId: String,
        version: Int,
        aggregateRootTypeName: String,
        events: List<DomainEventMessage>,
        items: MutableMap<String, Any>
    ) {
        this.commandId = commandId
        this.aggregateRootId = aggregateRootId
        this.aggregateRootTypeName = aggregateRootTypeName
        this.version = version
        this.events = events
        this.items = items
    }

    constructor(
        commandId: String,
        aggregateRootId: String,
        aggregateRootTypeName: String,
        timestamp: Date,
        events: List<DomainEventMessage>,
        items: MutableMap<String, Any>
    ) {
        require(events.isNotEmpty()) {
            String.format(
                "events cannot be empty. aggregateRootId: %s",
                aggregateRootId
            )
        }
        this.commandId = commandId
        this.aggregateRootId = aggregateRootId
        this.aggregateRootTypeName = aggregateRootTypeName
        this.version = events.stream().findFirst().map(DomainEventMessage::version).orElse(0)
        this.timestamp = timestamp
        this.events = events
        this.items = items
        this.id = aggregateRootId + "_" + version
        var sequence = 1
        for (event in events) {
            if (event.version != this.version) {
                throw DomainEventInvalidException(
                    String.format(
                        "Invalid domain event version, aggregateRootTypeName: %s aggregateRootId: %s expected version: %d, but was: %d",
                        aggregateRootTypeName,
                        aggregateRootId,
                        this.version,
                        event.version
                    )
                )
            }
            event.commandId = commandId
            event.aggregateRootTypeName = aggregateRootTypeName
            event.sequence = sequence++
            event.timestamp = timestamp
            event.mergeItems(items)
        }
    }
}
