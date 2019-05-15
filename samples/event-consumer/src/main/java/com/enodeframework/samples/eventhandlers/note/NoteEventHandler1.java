package com.enodeframework.samples.eventhandlers.note;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.infrastructure.IMessageHandler;
import com.enodeframework.samples.domain.note.NoteTitleChanged;
import com.enodeframework.samples.eventhandlers.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class NoteEventHandler1 implements IMessageHandler<NoteTitleChanged> {

    @Autowired
    TestService testService;

    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(NoteTitleChanged evnt) {
        testService.sayHello();
        System.out.println(String.format("Note denormalizered1, title：%s, Version: %d", evnt.getTitle(), evnt.version()));
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }
}
