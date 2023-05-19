package org.enodeframework.test.domain;

import org.enodeframework.eventing.AbstractDomainEventMessage;

public class TestAggregateTitleChanged extends AbstractDomainEventMessage {
    private String title;

    public TestAggregateTitleChanged() {
        super();
    }

    public TestAggregateTitleChanged(String title) {
        super();
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
