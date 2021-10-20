package org.enodeframework.common.utils;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;

/**
 * @author anruence@gmail.com
 */
public class ReplyUtil {

    private final static Logger logger = LoggerFactory.getLogger(ReplyUtil.class);

    public static Optional<URI> toURI(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return Optional.empty();
        }
        try {
            URI uri = new URI(value);
            return Optional.of(uri);
        } catch (URISyntaxException e) {
            logger.error("toSocketAddress error. uri: {}", value, e);
        }
        return Optional.empty();
    }

    public static String toUri(InetSocketAddress socketAddress) {
        if (Objects.isNull(socketAddress)) {
            return "";
        }
        return String.format("enode://%s:%d", socketAddress.getAddress().getHostAddress(), socketAddress.getPort());
    }

}
