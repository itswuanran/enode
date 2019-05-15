package com.enodeframework.samples.commandhandles.note;

import com.enodeframework.commanding.ICommandContext;
import com.enodeframework.commanding.ICommandHandler;
import com.enodeframework.samples.commands.note.ChangeNoteTitleCommand;
import com.enodeframework.samples.domain.note.Note;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class ChangeNoteTitleCommandHandler implements ICommandHandler<ChangeNoteTitleCommand> {
    private Logger logger = LoggerFactory.getLogger(ChangeNoteTitleCommandHandler.class);

    @Override
    public CompletableFuture<Note> handleAsync(ICommandContext context, ChangeNoteTitleCommand command) {
        logger.info(command.getTitle());
        CompletableFuture<Note> noteCompletableFuture = context.getAsync(command.getAggregateRootId(), true, Note.class);
        return noteCompletableFuture.thenApply(note -> {
            logger.info("note:{}", note.toString());
            note.changeTitle(command.getTitle());
            return note;
        });
    }
}
