package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class EnodeException extends RuntimeException {

    private static final long serialVersionUID = -8951926710590746149L;

    public EnodeException() {
        super();
    }

    public EnodeException(Throwable throwable) {
        super(throwable);
    }

    public EnodeException(String msg) {
        super(msg);
    }

    public EnodeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
