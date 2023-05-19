package org.enodeframework.domain

import org.enodeframework.messaging.Message

interface DomainExceptionMessage : Message {
    /**
     * Serialize the current exception info to the given dictionary.
     */
    fun serializeTo(serializableInfo: MutableMap<String, Any>)

    /**
     * Restore the current exception from the given dictionary.
     */
    fun restoreFrom(serializableInfo: MutableMap<String, Any>)
}
