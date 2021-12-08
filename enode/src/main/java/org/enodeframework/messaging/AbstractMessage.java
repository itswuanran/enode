package org.enodeframework.messaging;

import org.enodeframework.common.utils.IdGenerator;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractMessage implements Message {
    protected String id;
    protected Date timestamp;
    protected Map<String, Object> items;

    public AbstractMessage(String id) {
        this.id = id;
        this.timestamp = new Date();
        this.items = new HashMap<>();
    }

    public AbstractMessage() {
        this(IdGenerator.id());
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public Map<String, Object> getItems() {
        return items;
    }

    @Override
    public void setItems(Map<String, Object> items) {
        this.items = items;
    }
}
