package org.enodeframework.commanding

interface CommandProcessor {
    /**
     * Process the given command.
     */
    fun process(processingCommand: ProcessingCommand)

    /**
     * Start processor
     */
    fun start()

    /**
     * Stop processor
     */
    fun stop()
}