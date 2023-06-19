package org.enodeframework.queue.domainevent

import org.enodeframework.commanding.CommandReturnType
import org.enodeframework.commanding.CommandStatus
import org.enodeframework.messaging.AbstractReplyMessage

/**
 * @author anruence@gmail.com
 */
class DomainEventHandledMessage : AbstractReplyMessage() {
    override var returnType: CommandReturnType = CommandReturnType.EventHandled
    override var status: CommandStatus = CommandStatus.Success
    override var commandId: String = ""
    override var address: String = ""
    override var aggregateRootId: String = ""
    override var result: String = ""
}
