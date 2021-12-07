package org.enodeframework.test.command;

import org.enodeframework.commanding.AbstractCommandMessage;

public class AggregateThrowExceptionCommand extends AbstractCommandMessage<String> {
    public boolean publishableException;

    public boolean isPublishableException() {
        return publishableException;
    }

    public void setPublishableException(boolean publishableException) {
        this.publishableException = publishableException;
    }
}
