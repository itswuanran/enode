package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class ReplyAddressException extends EnodeRuntimeException {

    public ReplyAddressException() {
        super();
    }

    public ReplyAddressException(Throwable throwable) {
        super(throwable);
    }

    public ReplyAddressException(String msg) {
        super(msg);
    }

    public ReplyAddressException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
