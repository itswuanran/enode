package org.enodeframework.domain;

import com.google.common.collect.Lists;
import org.enodeframework.common.container.DefaultObjectContainer;
import org.enodeframework.common.function.Action2;
import org.enodeframework.common.utils.Assert;
import org.enodeframework.eventing.DomainEventMessage;
import org.enodeframework.eventing.DomainEventStream;

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Represents an abstract base aggregate root.
 *
 * @param <TAggregateRootId>
 */
public abstract class AbstractAggregateRoot<TAggregateRootId> implements AggregateRoot {
    private final List<DomainEventMessage<?>> emptyEvents = Collections.emptyList();
    protected TAggregateRootId id;
    protected int version;
    private Queue<DomainEventMessage<?>> uncommittedEvents = new ConcurrentLinkedQueue<>();

    protected AbstractAggregateRoot() {
    }

    protected AbstractAggregateRoot(TAggregateRootId id) {
        this(id, 0);
    }

    protected AbstractAggregateRoot(TAggregateRootId id, int version) {
        this();
        Assert.nonNull(id, "id");
        this.id = id;
        if (version < 0) {
            throw new IllegalArgumentException(String.format("Version cannot small than zero, aggregateRootId: %s, version: %d", id, version));
        }
        this.version = version;
    }

    public TAggregateRootId getId() {
        return this.id;
    }

    protected void applyEvent(DomainEventMessage<TAggregateRootId> domainEvent) {
        Assert.nonNull(domainEvent, "domainEvent");
        Assert.nonNull(id, "AggregateRootId");
        domainEvent.setAggregateRootId(id);
        domainEvent.setVersion(version + 1);
        handleEvent(domainEvent);
        appendUncommittedEvent(domainEvent);
    }

    protected void applyEvents(List<DomainEventMessage<TAggregateRootId>> domainEvents) {
        for (DomainEventMessage<TAggregateRootId> domainEvent : domainEvents) {
            applyEvent(domainEvent);
        }
    }

    private void handleEvent(DomainEventMessage<?> domainEvent) {
        Action2<AggregateRoot, DomainEventMessage<?>> handler = DefaultObjectContainer.resolve(AggregateRootInternalHandlerProvider.class).getInternalEventHandler(getClass(), (Class<? extends DomainEventMessage<?>>) domainEvent.getClass());
        if (this.id == null && domainEvent.getVersion() == 1) {
            this.id = (TAggregateRootId) domainEvent.getAggregateRootId();
        }
        handler.apply(this, domainEvent);
    }

    private void appendUncommittedEvent(DomainEventMessage<TAggregateRootId> domainEvent) {
        if (uncommittedEvents == null) {
            uncommittedEvents = new ConcurrentLinkedQueue<>();
        }
        if (uncommittedEvents.stream().anyMatch(x -> x.getClass().equals(domainEvent.getClass()))) {
            throw new UnsupportedOperationException(String.format("Cannot apply duplicated domain event type: %s, current aggregateRoot type: %s, id: %s", domainEvent.getClass(), this.getClass().getName(), id));
        }
        uncommittedEvents.add(domainEvent);
    }

    private void verifyEvent(DomainEventStream domainEventStream) {
        if (domainEventStream.getVersion() > 1 && !domainEventStream.getAggregateRootId().equals(this.getUniqueId())) {
            throw new UnsupportedOperationException(String.format("Invalid domain event stream, aggregateRootId:%s, expected aggregateRootId:%s, type:%s", domainEventStream.getAggregateRootId(), this.getUniqueId(), this.getClass().getName()));
        }
        if (domainEventStream.getVersion() != this.getVersion() + 1) {
            throw new UnsupportedOperationException(String.format("Invalid domain event stream, version:%d, expected version:%d, current aggregateRoot type:%s, id:%s", domainEventStream.getVersion(), this.getVersion(), this.getClass().getName(), this.getUniqueId()));
        }
    }

    @Override
    public String getUniqueId() {
        return id.toString();
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public List<DomainEventMessage<?>> getChanges() {
        if (uncommittedEvents == null) {
            return emptyEvents;
        }
        return Lists.newArrayList(uncommittedEvents);
    }

    @Override
    public void acceptChanges() {
        if (uncommittedEvents == null || uncommittedEvents.isEmpty()) {
            return;
        }
        version = uncommittedEvents.peek().getVersion();
        uncommittedEvents.clear();
    }

    @Override
    public void replayEvents(List<DomainEventStream> domainEventStreams) {
        if (domainEventStreams == null || domainEventStreams.isEmpty()) {
            return;
        }
        domainEventStreams.forEach(eventStream -> {
            verifyEvent(eventStream);
            eventStream.getEvents().forEach(this::handleEvent);
            this.version = eventStream.getVersion();
        });
    }
}
