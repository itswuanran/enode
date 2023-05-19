package org.enodeframework.commanding

import com.google.common.collect.Maps
import org.enodeframework.common.utils.Assert
import org.enodeframework.messaging.AbstractMessage
import java.util.*

/**
 * @author anruence@gmail.com
 */
abstract class AbstractCommandMessage : AbstractMessage, CommandMessage {
    constructor() : super()

    /**
     * Represents the timestamp of the message.
     */
    override var timestamp: Date = Date()

    override var aggregateRootId: String = ""

    /**
     * Represents the extension key/values data of the message.
     */
    override var items: MutableMap<String, Any> = Maps.newHashMap()

    @JvmOverloads
    constructor(aggregateRootId: String, items: MutableMap<String, Any> = Maps.newHashMap()) : super() {
        Assert.nonNull(aggregateRootId, "aggregateRootId")
        this.aggregateRootId = aggregateRootId
        this.items = items
    }

    /**
     * Init command with id and aggregateRootId
     */
    @JvmOverloads
    constructor(id: String, aggregateRootId: String, items: MutableMap<String, Any> = Maps.newHashMap()) : super(
        id
    ) {
        Assert.nonNull(aggregateRootId, "aggregateRootId")
        this.aggregateRootId = aggregateRootId
        this.items = items
    }
}
