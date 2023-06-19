package org.enodeframework.messaging

import com.google.common.collect.Maps
import java.util.*

/**
 * Represents an abstract reply message.
 */
abstract class AbstractReplyMessage : AbstractMessage(), ReplyMessage {

    override var timestamp: Date = Date()

    /**
     * Represents the extension key/values data of the message.
     */
    override var items: MutableMap<String, Any> = Maps.newHashMap()

}