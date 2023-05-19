package org.enodeframework.eventing

/**
 * @author anruence@gmail.com
 */
class ProcessingEvent(val message: DomainEventStream, val processContext: EventProcessContext) {
    var mailbox: ProcessingEventMailBox? = null

    fun complete() {
        processContext.notifyEventProcessed()
        if (mailbox != null) {
            mailbox!!.completeRun()
        }
    }
}
