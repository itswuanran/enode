package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class HandlerNotFoundException extends EnodeRuntimeException {

    public HandlerNotFoundException() {
        super();
    }

    public HandlerNotFoundException(String msg) {
        super(msg);
    }

    public HandlerNotFoundException(Throwable cause) {
        super(cause);
    }

    public HandlerNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
