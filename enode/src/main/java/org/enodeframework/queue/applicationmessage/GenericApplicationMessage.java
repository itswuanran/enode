package org.enodeframework.queue.applicationmessage;

/**
 * @author anruence@gmail.com
 */
public class GenericApplicationMessage {

    private String applicationMessageData;
    private String applicationMessageType;

    public GenericApplicationMessage() {
    }

    public GenericApplicationMessage(String applicationMessageData, String applicationMessageType) {
        this.applicationMessageData = applicationMessageData;
        this.applicationMessageType = applicationMessageType;
    }

    public String getApplicationMessageData() {
        return applicationMessageData;
    }

    public void setApplicationMessageData(String applicationMessageData) {
        this.applicationMessageData = applicationMessageData;
    }

    public String getApplicationMessageType() {
        return applicationMessageType;
    }

    public void setApplicationMessageType(String applicationMessageType) {
        this.applicationMessageType = applicationMessageType;
    }
}
