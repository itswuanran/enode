package org.enodeframework.test.command;

import org.enodeframework.commanding.AbstractCommandMessage;

public class CreateInheritTestAggregateCommand extends AbstractCommandMessage<String> {
    public String Title;

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }
}
