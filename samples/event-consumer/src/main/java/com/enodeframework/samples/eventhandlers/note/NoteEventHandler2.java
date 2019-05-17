package com.enodeframework.samples.eventhandlers.note;

import com.enodeframework.annotation.Event;
import com.enodeframework.annotation.Subscribe;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.samples.domain.note.NoteTitleChanged;
import com.enodeframework.samples.domain.note.NoteTitleChanged2;

import java.util.concurrent.CompletableFuture;

@Event
public class NoteEventHandler2 {

    @Subscribe
    public CompletableFuture<AsyncTaskResult> handleAsync(NoteTitleChanged evnt, NoteTitleChanged2 evnt2) {
        System.out.println(String.format("Note denormalizered, title：%s, Version: %d", evnt2.getTitle(), evnt.version()));
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }
}
