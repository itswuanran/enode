package org.enodeframework.samples.commands.note;

import org.enodeframework.commanding.AbstractCommandMessage;

public class CreateNoteCommand extends AbstractCommandMessage {
    private String title;

    public CreateNoteCommand() {
    }

    public CreateNoteCommand(String aggregateRootId, String title) {
        super(aggregateRootId);
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
