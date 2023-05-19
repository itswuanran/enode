package org.enodeframework.queue

/**
 * A enum defines the message type.
 */
enum class MessageTypeCode(var value: Char) {
    Default('0'),
    CommandMessage('1'),
    DomainEventMessage('2'),
    ExceptionMessage('3'),
    ApplicationMessage('4')
}