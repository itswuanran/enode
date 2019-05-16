package com.enodeframework.common.io;

import com.enodeframework.infrastructure.WrappedRuntimeException;

import java.util.concurrent.CompletableFuture;

public class Await {

    public static <T> T get(CompletableFuture<T> future) {
        try {
            return future.get();
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }
}
