package org.enodeframework.messaging

import java.util.*

interface Message {
    /**
     * Represents the unique identifier of the message.
     */
    var id: String

    /**
     * Represents the timestamp of the message.
     */
    var timestamp: Date

    /**
     * Represents the extension key/values data of the message.
     */
    var items: MutableMap<String, Any>

    /**
     * Merge the givens key/values into the current Items.
     */
    fun mergeItems(data: MutableMap<String, Any>) {
        if (data.isEmpty()) {
            return
        }
        items.putAll(data)
    }
}
