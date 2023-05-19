package org.enodeframework.spring;

import org.enodeframework.common.container.ObjectContainer;
import org.springframework.context.ApplicationContext;

/**
 * Spring Ioc容器
 *
 * @author anruence@gmail.com
 */
public class SpringObjectContainer implements ObjectContainer {

    public static String[] BASE_PACKAGES;
    private static ApplicationContext applicationContext;

    public SpringObjectContainer(ApplicationContext applicationContext) {
        SpringObjectContainer.applicationContext = applicationContext;
    }

    public static <TService> TService getBean(Class<TService> serviceType) {
        return applicationContext.getBean(serviceType);
    }

    @Override
    public <TService> TService resolve(Class<TService> serviceType) {
        return applicationContext.getBean(serviceType);
    }
}