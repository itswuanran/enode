package org.enodeframework.samples.domain.note;

import org.enodeframework.eventing.AbstractDomainEventMessage;

public class NoteCreated extends AbstractDomainEventMessage<String> {

    private String title;

    public NoteCreated() {

    }

    public NoteCreated(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
