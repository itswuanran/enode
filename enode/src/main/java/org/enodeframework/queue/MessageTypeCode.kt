package org.enodeframework.queue

/**
 * A enum defines the message type.
 */
enum class MessageTypeCode(var value: Char) {
    CommandMessage('1'),
    DomainEventMessage('2'),
    ExceptionMessage('3'),
    ApplicationMessage('4')
}