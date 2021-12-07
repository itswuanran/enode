package org.enodeframework.test.command;

import org.enodeframework.commanding.AbstractCommandMessage;

public class SetResultCommand extends AbstractCommandMessage<String> {
    public String Result;

    public String getResult() {
        return Result;
    }

    public void setResult(String result) {
        Result = result;
    }
}
