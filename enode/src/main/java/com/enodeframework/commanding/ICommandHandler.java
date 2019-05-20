package com.enodeframework.commanding;

public interface ICommandHandler<T extends ICommand> {
    /**
     * Handle the given aggregate command.
     *
     * @param context
     * @param command
     * @return
     */
    void handleAsync(ICommandContext context, T command);
}
