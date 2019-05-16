package com.enodeframework.samples.commandhandles.note;

import com.enodeframework.commanding.ICommandContext;
import com.enodeframework.commanding.ICommandHandler;
import com.enodeframework.samples.commands.note.CreateNoteCommand;
import com.enodeframework.samples.domain.note.Note;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class CreateNoteCommandHandler implements ICommandHandler<CreateNoteCommand> {

    private static Logger logger = LoggerFactory.getLogger(CreateNoteCommandHandler.class);

    /**
     * Handle the given aggregate command.
     *
     * @param context
     * @param command
     * @return
     */
    @Override
    public CompletableFuture handleAsync(ICommandContext context, CreateNoteCommand command) {
        Note note = new Note(command.getAggregateRootId(), command.getTitle());
        context.add(note);
        return CompletableFuture.completedFuture(note);
    }

}
