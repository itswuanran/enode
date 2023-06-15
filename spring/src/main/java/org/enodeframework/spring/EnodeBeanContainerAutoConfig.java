/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.enodeframework.spring;

import org.enodeframework.common.extensions.ClassNameComparator;
import org.enodeframework.common.extensions.ClassPathScanHandler;
import org.enodeframework.infrastructure.AssemblyInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Set;
import java.util.TreeSet;

public class EnodeBeanContainerAutoConfig implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(EnodeBeanContainerAutoConfig.class);

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        scanConfiguredPackages(SpringObjectContainer.BASE_PACKAGES);
    }

    private void registerBeans(Set<Class<?>> classSet) {
        applicationContext.getBeansOfType(AssemblyInitializer.class).values().forEach(provider -> {
            provider.initialize(new SpringObjectContainer(applicationContext), classSet);
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
