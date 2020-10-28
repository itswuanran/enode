package org.enodeframework.commanding;

/**
 * Represents a context environment for command executor executing command.
 */
public interface ICommandExecuteContext extends ICommandContext, ITrackingContext {
    /**
     * Notify the given command is executed.
     */
    void onCommandExecutedAsync(CommandResult commandResult);
}
