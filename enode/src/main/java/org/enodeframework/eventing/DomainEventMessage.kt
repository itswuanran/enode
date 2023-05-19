package org.enodeframework.eventing

import org.enodeframework.messaging.Message

interface DomainEventMessage : Message {

    var aggregateRootId: String

    var commandId: String

    var aggregateRootTypeName: String

    var version: Int

    var sequence: Int
}
