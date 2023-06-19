package org.enodeframework.queue

/**
 * enum defines the message type.
 */
enum class MessageTypeCode(var value: String) {
    Default(""),
    CommandMessage("command"),
    DomainEventMessage("event"),
    ExceptionMessage("exception"),
    ApplicationMessage("application"),
    ReplyMessage("reply"),
}