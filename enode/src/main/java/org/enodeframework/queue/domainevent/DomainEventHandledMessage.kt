package org.enodeframework.queue.domainevent

import java.io.Serializable

/**
 * @author anruence@gmail.com
 */
class DomainEventHandledMessage : Serializable {
    var commandId: String = ""
    var aggregateRootId: String = ""
    var commandResult: String = ""
}
