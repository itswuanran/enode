package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class AggregateRootTypeRepositoryNotFoundException extends EnodeException {

    public AggregateRootTypeRepositoryNotFoundException() {
        super();
    }

    public AggregateRootTypeRepositoryNotFoundException(String msg) {
        super(msg);
    }

    public AggregateRootTypeRepositoryNotFoundException(Throwable cause) {
        super(cause);
    }

    public AggregateRootTypeRepositoryNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
