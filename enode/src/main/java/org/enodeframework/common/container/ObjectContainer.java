package org.enodeframework.common.container;

import org.enodeframework.common.utilities.Ensure;

/**
 * @author anruence@gmail.com
 */
public class ObjectContainer {

    public static IObjectContainer INSTANCE;

    public static String[] BASE_PACKAGES;

    public static <T> T resolve(Class<T> targetClz) {
        Ensure.notNull(INSTANCE, "ObjectContainer can not be null");
        return INSTANCE.resolve(targetClz);
    }
}
