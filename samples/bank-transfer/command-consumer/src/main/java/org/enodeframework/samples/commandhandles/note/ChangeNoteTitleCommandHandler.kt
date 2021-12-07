package org.enodeframework.samples.commandhandles.note

import org.enodeframework.annotation.Command
import org.enodeframework.annotation.Subscribe
import org.enodeframework.commanding.CommandContext
import org.enodeframework.samples.commands.note.ChangeNoteTitleCommand
import org.enodeframework.samples.domain.note.Note

@Command
class ChangeNoteTitleCommandHandler {
    @Subscribe
    suspend fun handleAsync(context: CommandContext, command: ChangeNoteTitleCommand) {
        val note = context.get(command.aggregateRootId, true, Note::class.java)
        note.changeTitle(command.title)
    }
}