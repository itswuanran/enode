package org.enodeframework.eventing

interface ProcessingEventProcessor {
    /**
     * Process the given processingEvent.
     */
    fun process(processingEvent: ProcessingEvent)

    /**
     * Start the processor.
     */
    fun start()

    /**
     * Stop the processor.
     */
    fun stop()

    /**
     * The name of the processor
     */
    val name: String
}