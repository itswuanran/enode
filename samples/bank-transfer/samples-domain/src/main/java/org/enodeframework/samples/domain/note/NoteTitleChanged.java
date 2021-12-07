package org.enodeframework.samples.domain.note;

import org.enodeframework.eventing.AbstractDomainEventMessage;

public class NoteTitleChanged extends AbstractDomainEventMessage<String> {
    private String title;

    public NoteTitleChanged() {
    }

    public NoteTitleChanged(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
