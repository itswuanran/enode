package org.enodeframework.test.commandhandler;

import org.enodeframework.messaging.AbstractApplicationMessage;

public class TestApplicationMessage extends AbstractApplicationMessage {
    public String AggregateRootId;

    public TestApplicationMessage() {
    }

    public TestApplicationMessage(String aggregateRootId) {
        AggregateRootId = aggregateRootId;
    }

    public String GetRoutingKey() {
        return AggregateRootId;
    }
}
