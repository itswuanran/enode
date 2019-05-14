package com.enode.infrastructure;

import com.enode.common.utilities.ObjectId;

import java.util.Date;

public abstract class Message implements IMessage {
    public String id;
    public Date timestamp;
    public int sequence;

    public Message() {
        id = ObjectId.generateNewStringId();
        timestamp = new Date();
        sequence = 1;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Date timestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int sequence() {
        return sequence;
    }

    @Override
    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    @Override
    public String getRoutingKey() {
        return null;
    }

    @Override
    public String getTypeName() {
        return this.getClass().getName();
    }
}
