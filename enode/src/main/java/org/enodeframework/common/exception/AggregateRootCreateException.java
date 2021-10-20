package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class AggregateRootCreateException extends EnodeRuntimeException {

    public AggregateRootCreateException() {
        super();
    }

    public AggregateRootCreateException(String msg) {
        super(msg);
    }

    public AggregateRootCreateException(Throwable cause) {
        super(cause);
    }

    public AggregateRootCreateException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
