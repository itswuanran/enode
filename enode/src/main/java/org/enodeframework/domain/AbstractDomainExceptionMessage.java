package org.enodeframework.domain;

import org.enodeframework.common.exception.EnodeException;
import org.enodeframework.common.utils.IdGenerator;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractDomainExceptionMessage extends EnodeException implements DomainExceptionMessage {
    private String id;
    private Date timestamp;
    private Map<String, Object> items;

    public AbstractDomainExceptionMessage() {
        this(IdGenerator.id());
    }

    public AbstractDomainExceptionMessage(String id) {
        this.id = id;
        this.timestamp = new Date();
        this.items = new HashMap<>();
    }

    @Override
    public abstract void serializeTo(Map<String, Object> serializableInfo);

    @Override
    public abstract void restoreFrom(Map<String, Object> serializableInfo);

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

    @Override
    public void mergeItems(Map<String, Object> mitems) {
        if (mitems == null || mitems.size() == 0) {
            return;
        }
        if (this.items == null) {
            this.items = new HashMap<>();
        }
        for (Map.Entry<String, Object> entry : mitems.entrySet()) {
            if (!this.items.containsKey(entry.getKey())) {
                this.items.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
