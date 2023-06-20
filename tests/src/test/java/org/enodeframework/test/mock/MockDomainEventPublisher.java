package org.enodeframework.test.mock;

import org.enodeframework.common.exception.EnodeException;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.eventing.DomainEventStream;
import org.enodeframework.messaging.MessagePublisher;
import org.enodeframework.queue.SendMessageResult;

import java.util.concurrent.CompletableFuture;

public class MockDomainEventPublisher implements MessagePublisher<DomainEventStream> {
    private static final CompletableFuture<SendMessageResult> successResultTask = CompletableFuture.completedFuture(new SendMessageResult(""));

    private int expectFailedCount = 0;
    private int currentFailedCount = 0;
    private FailedType failedType;

    public void Reset() {
        failedType = FailedType.None;
        expectFailedCount = 0;
        currentFailedCount = 0;
    }

    public void setExpectFailedCount(FailedType failedType, int count) {
        this.failedType = failedType;
        expectFailedCount = count;
    }

    @Override
    public CompletableFuture<SendMessageResult> publishAsync(DomainEventStream message) {
        if (currentFailedCount < expectFailedCount) {
            currentFailedCount++;
            if (failedType == FailedType.UnKnownException) {
                throw new EnodeException("PublishDomainEventStreamMessageAsyncUnKnownException" + currentFailedCount);
            } else if (failedType == FailedType.IOException) {
                throw new IORuntimeException("PublishDomainEventStreamMessageAsyncIOException" + currentFailedCount);
            } else if (failedType == FailedType.TaskIOException) {
            }
        }
        return successResultTask;
    }
}
