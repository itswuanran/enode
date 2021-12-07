package org.enodeframework.domain;

import org.enodeframework.messaging.Message;

import java.util.Map;

public interface DomainExceptionMessage extends Message {
    /**
     * Serialize the current exception info to the given dictionary.
     */
    void serializeTo(Map<String, Object> serializableInfo);

    /**
     * Restore the current exception from the given dictionary.
     */
    void restoreFrom(Map<String, Object> serializableInfo);
}
