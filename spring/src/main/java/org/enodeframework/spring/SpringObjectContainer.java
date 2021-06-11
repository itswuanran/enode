package org.enodeframework.spring;

import org.enodeframework.common.container.IObjectContainer;
import org.springframework.context.ApplicationContext;

/**
 * Spring Ioc容器
 *
 * @author anruence@gmail.com
 */
public class SpringObjectContainer implements IObjectContainer {

    private final ApplicationContext applicationContext;

    public SpringObjectContainer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public <TService> TService resolve(Class<TService> serviceType) {
        return applicationContext.getBean(serviceType);
    }

}
