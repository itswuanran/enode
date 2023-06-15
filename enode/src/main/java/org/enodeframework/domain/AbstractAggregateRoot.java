/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.enodeframework.domain;

import com.google.common.collect.Lists;
import org.enodeframework.common.function.Action2;
import org.enodeframework.common.utils.Assert;
import org.enodeframework.domain.impl.DefaultAggregateRootInternalHandlerProvider;
import org.enodeframework.eventing.DomainEventMessage;
import org.enodeframework.eventing.DomainEventStream;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Represents an abstract base aggregate root.
 */
public abstract class AbstractAggregateRoot implements AggregateRoot {
    private final List<DomainEventMessage> emptyEvents = Collections.emptyList();
    protected String id;
    protected int version;
    private Queue<DomainEventMessage> uncommittedEvents = new ConcurrentLinkedQueue<>();

    protected AbstractAggregateRoot() {
    }

    protected AbstractAggregateRoot(String id) {
        this(id, 0);
    }

    protected AbstractAggregateRoot(String id, int version) {
        this();
        Assert.nonNull(id, "id");
        this.id = id;
        if (version < 0) {
            throw new IllegalArgumentException(
                String.format("Version cannot small than zero, aggregateRootId: %s, version: %d", id, version));
        }
        this.version = version;
    }

    public String getId() {
        return this.id;
    }

    protected void applyEvent(DomainEventMessage domainEvent) {
        Assert.nonNull(domainEvent, "domainEvent");
        Assert.nonNull(id, "AggregateRootId");
        domainEvent.setAggregateRootId(id);
        domainEvent.setVersion(version + 1);
        handleEvent(domainEvent);
        appendUncommittedEvent(domainEvent);
    }

    protected void applyEvents(List<DomainEventMessage> domainEvents) {
        for (DomainEventMessage domainEvent : domainEvents) {
            applyEvent(domainEvent);
        }
    }

    private void handleEvent(DomainEventMessage domainEvent) {
        Action2<AggregateRoot, DomainEventMessage> handler =
            DefaultAggregateRootInternalHandlerProvider.Dict.getInternalEventHandler(
                getClass(), domainEvent.getClass());
        if (this.id == null && domainEvent.getVersion() == 1) {
            this.id = domainEvent.getAggregateRootId();
        }
        handler.apply(this, domainEvent);
    }

    private void appendUncommittedEvent(DomainEventMessage domainEvent) {
        if (uncommittedEvents == null) {
            uncommittedEvents = new ConcurrentLinkedQueue<>();
        }
        if (uncommittedEvents.stream().anyMatch(x -> x.getClass().equals(domainEvent.getClass()))) {
            throw new UnsupportedOperationException(String.format(
                "Cannot apply duplicated domain event type: %s, current aggregateRoot type: %s, id: %s",
                domainEvent.getClass(), this.getClass().getName(), id));
        }
        uncommittedEvents.add(domainEvent);
    }

    private void verifyEvent(DomainEventStream domainEventStream) {
        if (domainEventStream.getVersion() > 1
            && !domainEventStream.getAggregateRootId().equals(this.id)) {
            throw new UnsupportedOperationException(String.format(
                "Invalid domain event stream, aggregateRootId:%s, expected aggregateRootId:%s, type:%s",
                domainEventStream.getAggregateRootId(),
                this.id,
                this.getClass().getName()));
        }
        if (domainEventStream.getVersion() != this.getVersion() + 1) {
            throw new UnsupportedOperationException(String.format(
                "Invalid domain event stream, version:%d, expected version:%d, current aggregateRoot type:%s, id:%s",
                domainEventStream.getVersion(),
                this.getVersion(),
                this.getClass().getName(),
                this.id));
        }
    }

    @NotNull
    @Override
    public String getUniqueId() {
        return Objects.toString(id, "");
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public List<DomainEventMessage> getChanges() {
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
