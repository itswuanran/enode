package org.enodeframework.common.utils;

import io.vertx.core.net.SocketAddress;
import org.enodeframework.common.exception.EnodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Objects;

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

    public static String toAddr(SocketAddress socketAddress) {
        if (Objects.isNull(socketAddress)) {
            return "";
        }
        return String.format("enode://%s:%d", socketAddress.hostAddress(), socketAddress.port());
    }
}
