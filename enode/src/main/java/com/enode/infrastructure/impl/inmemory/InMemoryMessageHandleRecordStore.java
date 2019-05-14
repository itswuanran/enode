package com.enode.infrastructure.impl.inmemory;

import com.enode.common.io.AsyncTaskResult;
import com.enode.common.io.AsyncTaskStatus;
import com.enode.infrastructure.IMessageHandleRecordStore;
import com.enode.infrastructure.MessageHandleRecord;
import com.enode.infrastructure.ThreeMessageHandleRecord;
import com.enode.infrastructure.TwoMessageHandleRecord;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryMessageHandleRecordStore implements IMessageHandleRecordStore {
    private final CompletableFuture<AsyncTaskResult> successTask = CompletableFuture.completedFuture(AsyncTaskResult.Success);
    private final ConcurrentMap<String, Integer> dict = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<AsyncTaskResult> addRecordAsync(MessageHandleRecord record) {
        dict.put(record.getMessageId() + record.getHandlerTypeName(), 0);
        return successTask;
    }

    @Override
    public CompletableFuture<AsyncTaskResult> addRecordAsync(TwoMessageHandleRecord record) {
        dict.put(record.getMessageId1() + record.getMessageId2() + record.getHandlerTypeName(), 0);
        return successTask;
    }

    @Override
    public CompletableFuture<AsyncTaskResult> addRecordAsync(ThreeMessageHandleRecord record) {
        dict.put(record.getMessageId1() + record.getMessageId2() + record.getMessageId3() + record.getHandlerTypeName(), 0);
        return successTask;
    }

    @Override
    public CompletableFuture<AsyncTaskResult<Boolean>> isRecordExistAsync(String messageId, String handlerTypeName, String aggregateRootTypeName) {
        return CompletableFuture.completedFuture(new AsyncTaskResult<>(AsyncTaskStatus.Success, dict.containsKey(messageId + handlerTypeName)));
    }

    @Override
    public CompletableFuture<AsyncTaskResult<Boolean>> isRecordExistAsync(String messageId1, String messageId2, String handlerTypeName, String aggregateRootTypeName) {
        return CompletableFuture.completedFuture(new AsyncTaskResult<>(AsyncTaskStatus.Success, dict.containsKey(messageId1 + messageId2 + handlerTypeName)));
    }

    @Override
    public CompletableFuture<AsyncTaskResult<Boolean>> isRecordExistAsync(String messageId1, String messageId2, String messageId3, String handlerTypeName, String aggregateRootTypeName) {
        return CompletableFuture.completedFuture(new AsyncTaskResult<>(AsyncTaskStatus.Success, dict.containsKey(messageId1 + messageId2 + messageId3 + handlerTypeName)));
    }
}
