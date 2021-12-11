package org.enodeframework.queue.applicationmessage;

import java.io.Serializable;

/**
 * @author anruence@gmail.com
 */
public class GenericApplicationMessage implements Serializable {

    private String applicationMessageData;

    private String applicationMessageType;

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
