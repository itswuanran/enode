package org.enodeframework.queue.command

import org.enodeframework.commanding.CommandReturnType
import org.enodeframework.commanding.CommandStatus
import org.enodeframework.messaging.AbstractReplyMessage

/**
 * @author anruence@gmail.com
 */
class CommandHandledMessage : AbstractReplyMessage() {

    override var returnType: CommandReturnType = CommandReturnType.CommandExecuted
    override var status: CommandStatus = CommandStatus.Success
    override var commandId: String = ""
    override var address: String = ""
    override var aggregateRootId: String = ""
    override var result: String = ""
}