package org.enodeframework.test.repository;

import org.enodeframework.domain.AggregateRepository;
import org.enodeframework.test.domain.TestAggregate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class TestAggregateRepository implements AggregateRepository<TestAggregate> {
    @Override
    public CompletableFuture<TestAggregate> getAsync(String aggregateRootId) {
        return CompletableFuture.completedFuture(null);
    }
}
