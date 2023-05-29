package org.enodeframework.common.utils;

import org.enodeframework.common.exception.EnodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * @author anruence@gmail.com
 */
public class ReplyUtil {
    private final static Logger logger = LoggerFactory.getLogger(ReplyUtil.class);

    public static URI toURI(String value) {
        try {
            return new URI(value);
        } catch (Exception e) {
            logger.error("toURI error. uri: {}", value, e);
            throw new EnodeException(e);
        }
    }
}
