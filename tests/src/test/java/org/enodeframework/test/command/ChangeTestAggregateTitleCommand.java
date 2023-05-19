package org.enodeframework.test.command;

import org.enodeframework.commanding.AbstractCommandMessage;

public class ChangeTestAggregateTitleCommand extends AbstractCommandMessage {
    public String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
