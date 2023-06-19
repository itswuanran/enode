package org.enodeframework.queue.reply

import org.enodeframework.commanding.CommandResult
import org.enodeframework.commanding.CommandReturnType
import org.enodeframework.commanding.CommandStatus
import java.io.Serializable

/**
 * @author anruence@gmail.com
 */
class GenericReplyMessage : Serializable {
    var id: String = ""
    var returnType: Int = CommandReturnType.EventHandled.value
    var status: String = CommandStatus.Failed.value
    var commandId: String = ""
    var address: String = ""
    var aggregateRootId: String = ""
    var result: String = ""
    fun asCommandResult(): CommandResult {
        return CommandResult(CommandStatus.valueOf(status), commandId, aggregateRootId, result)
    }
}