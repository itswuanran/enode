package org.enodeframework.commanding

import org.enodeframework.infrastructure.IObjectProxy
import org.enodeframework.infrastructure.MethodInvocation

interface ICommandHandlerProxy : IObjectProxy, MethodInvocation {
    /**
     * Handle the given application command async. deal with aggregate in memory
     */
    suspend fun handleAsync(context: ICommandContext, command: ICommand)
}