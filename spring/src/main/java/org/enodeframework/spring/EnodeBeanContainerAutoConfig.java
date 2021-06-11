package org.enodeframework.spring;

import org.enodeframework.common.container.ObjectContainer;
import org.enodeframework.common.extensions.ClassNameComparator;
import org.enodeframework.common.extensions.ClassPathScanHandler;
import org.enodeframework.infrastructure.IAssemblyInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Set;
import java.util.TreeSet;

public class EnodeBeanContainerAutoConfig implements ApplicationContextAware {

    private final static Logger logger = LoggerFactory.getLogger(EnodeBeanContainerAutoConfig.class);

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        ObjectContainer.INSTANCE = new SpringObjectContainer(applicationContext);
        scanConfiguredPackages(ObjectContainer.BASE_PACKAGES);
    }

    private void registerBeans(Set<Class<?>> classSet) {
        applicationContext.getBeansOfType(IAssemblyInitializer.class).values().forEach(provider -> {
            provider.initialize(classSet);
            if (logger.isDebugEnabled()) {
                logger.debug("{} initialize success", provider.getClass().getName());
            }
        });
    }

    /**
     * Scan the packages configured
     */
    private void scanConfiguredPackages(String... scanPackages) {
        if (scanPackages == null) {
            throw new IllegalArgumentException("packages is not specified");
        }
        ClassPathScanHandler handler = new ClassPathScanHandler(scanPackages);
        Set<Class<?>> classSet = new TreeSet<>(new ClassNameComparator());
        for (String pakName : scanPackages) {
            classSet.addAll(handler.getPackageAllClasses(pakName, true));
        }
        this.registerBeans(classSet);
    }
}
