package org.enodeframework.messaging;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public interface Message {
    /**
     * Represents the unique identifier of the message.
     */
    String getId();

    void setId(String id);

    /**
     * Represents the timestamp of the message.
     */
    Date getTimestamp();

    void setTimestamp(Date timestamp);

    /**
     * Represents the extension key/values data of the message.
     */
    Map<String, Object> getItems();


    void setItems(Map<String, Object> items);

    /**
     * Merge the givens key/values into the current Items.
     */
    default void mergeItems(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        if (this.getItems() == null) {
            this.setItems(new HashMap<>(data));
            return;
        }
        this.getItems().putAll(data);
    }
}
