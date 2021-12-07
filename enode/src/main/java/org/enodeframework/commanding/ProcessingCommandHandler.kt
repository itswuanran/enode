package org.enodeframework.commanding

import java.util.concurrent.CompletableFuture

interface ProcessingCommandHandler {
    /**
     * process given processing command.
     */
    fun handleAsync(processingCommand: ProcessingCommand): CompletableFuture<Boolean>
}