package org.enodeframework.common.utils;

import org.enodeframework.common.extensions.SnowFlake;

import java.util.Objects;

/**
 * @author anruence@gmail.com
 */
public class IdGenerator {

    private static final SnowFlake IDENTIFIER_GENERATOR = new SnowFlake();

    public static String id() {
        return Objects.toString(lid(), "");
    }

    public static long lid() {
        return IDENTIFIER_GENERATOR.nextId();
    }
}
