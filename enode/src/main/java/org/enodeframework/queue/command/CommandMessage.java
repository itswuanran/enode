package org.enodeframework.queue.command;

import java.io.Serializable;

/**
 * @author anruence@gmail.com
 */
public class CommandMessage implements Serializable {
    private String commandType;
    private String commandData;
    private String replyAddress;

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getCommandData() {
        return commandData;
    }

    public void setCommandData(String commandData) {
        this.commandData = commandData;
    }

    public String getReplyAddress() {
        return replyAddress;
    }

    public void setReplyAddress(String replyAddress) {
        this.replyAddress = replyAddress;
    }

}
