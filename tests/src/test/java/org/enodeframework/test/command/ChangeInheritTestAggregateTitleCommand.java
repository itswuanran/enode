package org.enodeframework.test.command;

import org.enodeframework.commanding.AbstractCommandMessage;

public class ChangeInheritTestAggregateTitleCommand extends AbstractCommandMessage {
    public String Title;

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }
}