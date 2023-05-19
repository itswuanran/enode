package org.enodeframework.messaging

import com.google.common.collect.Maps
import java.util.*

/**
 * Represents an abstract application message.
 */
abstract class AbstractApplicationMessage : AbstractMessage(), ApplicationMessage {

    override var timestamp: Date = Date()

    /**
     * Represents the extension key/values data of the message.
     */
    override var items: MutableMap<String, Any> = Maps.newHashMap()

}