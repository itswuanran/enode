package org.enodeframework.messaging

import org.enodeframework.commanding.CommandReturnType
import org.enodeframework.commanding.CommandStatus
import org.enodeframework.queue.MessageTypeCode
import org.enodeframework.queue.QueueMessage
import org.enodeframework.queue.reply.GenericReplyMessage

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

    fun asGenericReplyMessage(): GenericReplyMessage {
        val message = GenericReplyMessage()
        message.id = this.id
        message.result = this.result
        message.commandId = this.commandId
        message.aggregateRootId = this.aggregateRootId
        message.address = this.address
        message.status = this.status.value
        message.returnType = this.returnType.value
        return message
    }

    fun asPartQueueMessage(): QueueMessage {
        val queueMessage = QueueMessage()
        queueMessage.key = "${this.commandId}_agg_cmd_${this.aggregateRootId}"
        queueMessage.routeKey = this.commandId
        queueMessage.tag = this.address
        queueMessage.type = MessageTypeCode.ReplyMessage.value
        return queueMessage
    }
}

