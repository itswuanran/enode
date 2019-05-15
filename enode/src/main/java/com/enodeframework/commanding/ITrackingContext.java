package com.enodeframework.commanding;

import com.enodeframework.domain.IAggregateRoot;

import java.util.List;

public interface ITrackingContext {
    List<IAggregateRoot> getTrackedAggregateRoots();

    void clear();
}
