package org.enodeframework.commanding

import org.enodeframework.infrastructure.MethodInvocation
import org.enodeframework.infrastructure.ObjectProxy

interface CommandHandlerProxy : ObjectProxy, MethodInvocation {
    /**
     * Handle the given application command async. deal with aggregate in memory
     */
    suspend fun handleAsync(context: CommandContext, command: CommandMessage)
}