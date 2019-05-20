package com.enodeframework.common.container;

/**
 * 抽象的Ioc容器，Dependency Inject
 */
public interface IObjectContainer {

    <TService> TService resolve(Class<TService> serviceType);

}
