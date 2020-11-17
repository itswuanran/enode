package org.enodeframework.test.command;

import org.enodeframework.commanding.Command;

public class AggregateThrowExceptionCommand extends Command<String> {
    public boolean publishableException;

    public boolean isPublishableException() {
        return publishableException;
    }

    public void setPublishableException(boolean publishableException) {
        this.publishableException = publishableException;
    }
}
