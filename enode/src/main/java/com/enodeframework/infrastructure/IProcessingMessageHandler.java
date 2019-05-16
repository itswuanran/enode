package com.enodeframework.infrastructure;

import java.util.concurrent.CompletableFuture;

public interface IProcessingMessageHandler<X extends IProcessingMessage<X, Y>, Y extends IMessage> {
    CompletableFuture handleAsync(X processingMessage);
}