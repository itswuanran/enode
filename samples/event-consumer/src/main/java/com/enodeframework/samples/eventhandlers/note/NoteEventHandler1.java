package com.enodeframework.samples.eventhandlers.note;

import com.enodeframework.annotation.Event;
import com.enodeframework.annotation.Subscribe;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.samples.domain.note.NoteTitleChanged;
import com.enodeframework.samples.eventhandlers.TestService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

@Event
public class NoteEventHandler1 {
    @Autowired
    TestService testService;

    @Subscribe
    public CompletableFuture<AsyncTaskResult> handleAsync(NoteTitleChanged evnt) {
        testService.sayHello();
        System.out.println(String.format("Note denormalizered1, title：%s, Version: %d", evnt.getTitle(), evnt.version()));
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }
}
