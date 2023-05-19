package org.enodeframework.common.container;

/**
 * 抽象的Ioc容器，Dependency Inject
 */
public interface ObjectContainer {
    <TService> TService resolve(Class<TService> serviceType);
}
