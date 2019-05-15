package com.enodeframework.eventing;

import com.enodeframework.common.IEnum;

public enum EventAppendResult implements IEnum {
    Success(1),
    Failed(2),
    DuplicateEvent(3),
    DuplicateCommand(4);

    private int status;

    EventAppendResult(int status) {
        this.status = status;
    }

    @Override
    public int status() {
        return status;
    }
}
