package org.enodeframework.domain;

import org.enodeframework.eventing.DomainEventMessage;
import org.enodeframework.eventing.DomainEventStream;

import java.util.List;

/**
 * Represents an aggregate root.
 */
public interface AggregateRoot {
    /**
     * Represents the unique id of the aggregate root.
     */
    String getUniqueId();

    /**
     * Represents the current version of the aggregate root.
     */
    int getVersion();

    /**
     * Get all the changes of the aggregate root.
     */
    List<DomainEventMessage<?>> getChanges();

    /**
     * Accept changes of the aggregate root.
     */
    void acceptChanges();

    /**
     * Replay the given event streams.
     */
    void replayEvents(List<DomainEventStream> eventStreams);
}
