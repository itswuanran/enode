package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class PublishedVersionStoreException extends EnodeRuntimeException {

    public PublishedVersionStoreException() {
        super();
    }

    public PublishedVersionStoreException(String msg) {
        super(msg);
    }

    public PublishedVersionStoreException(Throwable cause) {
        super(cause);
    }

    public PublishedVersionStoreException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
