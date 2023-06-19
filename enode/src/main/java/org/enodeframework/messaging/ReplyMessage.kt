package org.enodeframework.messaging

import org.enodeframework.commanding.CommandReturnType
import org.enodeframework.commanding.CommandStatus

/**
 * Represents an application message.
 */
interface ReplyMessage : Message {

    var commandId: String

    var aggregateRootId: String

    /**
     * Represents the command message address
     */
    var address: String

    /**
     * Represents the command result data.
     */
    var result: String

    var returnType: CommandReturnType

    var status: CommandStatus
}

