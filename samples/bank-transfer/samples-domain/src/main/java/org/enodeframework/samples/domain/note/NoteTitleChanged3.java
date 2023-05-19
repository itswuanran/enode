package org.enodeframework.samples.domain.note;

import org.enodeframework.eventing.AbstractDomainEventMessage;

public class NoteTitleChanged3 extends AbstractDomainEventMessage {
    private String title;

    public NoteTitleChanged3() {
    }

    public NoteTitleChanged3(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
