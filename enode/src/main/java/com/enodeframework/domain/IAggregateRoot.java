package com.enodeframework.domain;

import com.enodeframework.eventing.DomainEventStream;
import com.enodeframework.eventing.IDomainEvent;

import java.util.List;

/**
 * Represents an aggregate root.
 */
public interface IAggregateRoot {
    String uniqueId();

    int version();

    List<IDomainEvent> getChanges();

    void acceptChanges(int newVersion);

    void replayEvents(List<DomainEventStream> eventStreams);
}
