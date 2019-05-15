package com.enodeframework.domain.impl;

import com.enodeframework.domain.IAggregateRoot;
import com.enodeframework.domain.IAggregateRootFactory;
import com.enodeframework.infrastructure.WrappedRuntimeException;

public class DefaultAggregateRootFactory implements IAggregateRootFactory {

    @Override
    public <T extends IAggregateRoot> T createAggregateRoot(Class<T> aggregateRootType) {
        try {
            return aggregateRootType.newInstance();
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }
}
