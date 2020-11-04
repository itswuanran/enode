package org.enodeframework.common.utilities;

import com.google.common.base.Strings;

/**
 * @author anruence@gmail.com
 */
public class Ensure {
    public static <T> void notNull(T argument, String argumentName) {
        if (argument == null) {
            throw new IllegalArgumentException(String.format("%s should not be null.", argumentName));
        }
    }

    public static void notNullOrEmpty(String argument, String argumentName) {
        if (Strings.isNullOrEmpty(argument)) {
            throw new IllegalArgumentException(String.format("%s should not be null or empty.", argumentName));
        }
    }

    public static void equal(int expected, int actual, String argumentName) {
        if (expected != actual) {
            throw new IllegalArgumentException(String.format("%s expected value: %d, actual value: %d", argumentName, expected, actual));
        }
    }

    public static void equal(long expected, long actual, String argumentName) {
        if (expected != actual) {
            throw new IllegalArgumentException(String.format("%s expected value: %d, actual value: %d", argumentName, expected, actual));
        }
    }

    public static void equal(boolean expected, boolean actual, String argumentName) {
        if (expected != actual) {
            throw new IllegalArgumentException(String.format("%s expected value: %d, actual value: %d", argumentName, expected, actual));
        }
    }
}
