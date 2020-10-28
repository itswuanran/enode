package org.enodeframework.queue.domainevent;

import java.io.Serializable;

/**
 * @author anruence@gmail.com
 */
public class DomainEventHandledMessage implements Serializable {
    private String commandId;
    private String aggregateRootId;
    private String commandResult;

    public String getCommandId() {
        return commandId;
    }

    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }

    public String getAggregateRootId() {
        return aggregateRootId;
    }

    public void setAggregateRootId(String aggregateRootId) {
        this.aggregateRootId = aggregateRootId;
    }

    public String getCommandResult() {
        return commandResult;
    }

    public void setCommandResult(String commandResult) {
        this.commandResult = commandResult;
    }
}
