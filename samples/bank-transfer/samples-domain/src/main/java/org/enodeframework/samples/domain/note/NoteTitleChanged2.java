package org.enodeframework.samples.domain.note;

import org.enodeframework.eventing.AbstractDomainEventMessage;

public class NoteTitleChanged2 extends AbstractDomainEventMessage {
    private String title;

    public NoteTitleChanged2() {
    }

    public NoteTitleChanged2(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
