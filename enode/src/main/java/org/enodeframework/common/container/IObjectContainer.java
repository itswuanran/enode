package org.enodeframework.common.container;

import java.util.Map;

/**
 * 抽象的Ioc容器，Dependency Inject
 */
public interface IObjectContainer {

    <TService> TService resolve(Class<TService> serviceType);
}
