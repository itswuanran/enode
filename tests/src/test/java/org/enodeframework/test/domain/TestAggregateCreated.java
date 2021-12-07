package org.enodeframework.test.domain;

import org.enodeframework.eventing.AbstractDomainEventMessage;


public class TestAggregateCreated extends AbstractDomainEventMessage<String> {
    private String title;

    public TestAggregateCreated() {
    }

    public TestAggregateCreated(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
