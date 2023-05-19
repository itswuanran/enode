package org.enodeframework.test.mock;

import org.enodeframework.common.io.Task;
import org.enodeframework.eventing.DomainEventStream;
import org.enodeframework.eventing.EventProcessContext;
import org.enodeframework.test.async.ManualResetEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DomainEventStreamProcessContext implements EventProcessContext {
    private final DomainEventStream domainEventStreamMessage;
    private final ManualResetEvent manualResetEvent;
    private final List<Integer> versionList;

    public DomainEventStreamProcessContext(DomainEventStream domainEventStreamMessage, ManualResetEvent waitHandle, List<Integer> versionList) {
        this.domainEventStreamMessage = domainEventStreamMessage;
        manualResetEvent = waitHandle;
        this.versionList = versionList;
    }

    @Override
    public CompletableFuture<Boolean> notifyEventProcessed() {
        versionList.add(domainEventStreamMessage.getVersion());
        if (domainEventStreamMessage.getVersion() == 3) {
            manualResetEvent.set();
        }
        return Task.completedTask;
    }
}
