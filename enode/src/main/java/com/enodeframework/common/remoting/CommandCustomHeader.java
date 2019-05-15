package com.enodeframework.common.remoting;

import com.enodeframework.common.remoting.exception.RemotingCommandException;

public interface CommandCustomHeader {
    void checkFields() throws RemotingCommandException;
}
