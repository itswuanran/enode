package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class ReplyAddressInvalidException extends EnodeException {

    public ReplyAddressInvalidException() {
        super();
    }

    public ReplyAddressInvalidException(Throwable throwable) {
        super(throwable);
    }

    public ReplyAddressInvalidException(String msg) {
        super(msg);
    }

    public ReplyAddressInvalidException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
