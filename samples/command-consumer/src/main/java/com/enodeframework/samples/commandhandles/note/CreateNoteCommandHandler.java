package com.enodeframework.samples.commandhandles.note;

import com.enodeframework.annotation.Command;
import com.enodeframework.commanding.ICommandContext;
import com.enodeframework.commanding.ICommandHandler;
import com.enodeframework.samples.commands.note.CreateNoteCommand;
import com.enodeframework.samples.domain.note.Note;

import java.util.concurrent.CompletableFuture;

@Command
public class CreateNoteCommandHandler implements ICommandHandler<CreateNoteCommand> {
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
