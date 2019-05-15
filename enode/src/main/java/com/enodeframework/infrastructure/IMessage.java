package com.enodeframework.infrastructure;

import java.util.Date;

public interface IMessage {
    String id();

    void setId(String id);

    Date timestamp();

    void setTimestamp(Date timestamp);

    int sequence();

    void setSequence(int sequence);

    String getRoutingKey();

    String getTypeName();
}
