package org.enodeframework.common.utils;

import com.google.common.base.Strings;
import io.vertx.core.net.SocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * @author anruence@gmail.com
 */
public class ReplyUtil {

    private final static Logger logger = LoggerFactory.getLogger(ReplyUtil.class);

    public static SocketAddress toSocketAddress(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }
        try {
            URI uri = new URI(value);
            return SocketAddress.inetSocketAddress(uri.getPort(), uri.getHost());
        } catch (URISyntaxException e) {
            logger.error("toSocketAddress error. uri: {}", value, e);
        }
        return null;
    }

    public static String toURI(SocketAddress socketAddress) {
        if (Objects.isNull(socketAddress)) {
            return "";
        }
        return String.format("enode://%s:%d", socketAddress.hostAddress(), socketAddress.port());
    }
}
