package org.enodeframework.eventing;

import org.enodeframework.messaging.Message;

public interface DomainEventMessage<TAggregateRootId> extends Message {
    TAggregateRootId getAggregateRootId();

    void setAggregateRootId(TAggregateRootId aggregateRootId);

    String getCommandId();

    void setCommandId(String commandId);

    String getAggregateRootTypeName();

    void setAggregateRootTypeName(String aggregateRootTypeName);

    int getVersion();

    void setVersion(int version);

    int getSequence();

    void setSequence(int sequence);
}
