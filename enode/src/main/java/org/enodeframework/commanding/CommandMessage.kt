package org.enodeframework.commanding

import org.enodeframework.messaging.Message

/**
 * Represents a command.
 */
interface CommandMessage : Message {
    /**
     * Represents the associated aggregate root string id.
     */
    var aggregateRootId: String

}