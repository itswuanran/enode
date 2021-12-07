package org.enodeframework.messaging;

import java.util.concurrent.CompletableFuture;

public interface MessagePublisher<TMessage extends Message> {
    CompletableFuture<Boolean> publishAsync(TMessage message);
}
