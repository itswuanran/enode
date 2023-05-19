package org.enodeframework.samples.commands.note;

import org.enodeframework.commanding.AbstractCommandMessage;

public class ChangeNoteTitleCommand extends AbstractCommandMessage {
    private String title;

    public ChangeNoteTitleCommand() {
    }

    public ChangeNoteTitleCommand(String aggregateRootId, String title) {
        super(aggregateRootId);
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
