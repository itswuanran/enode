package org.enodeframework.eventing;

/**
 * @author anruence@gmail.com
 */
public class ProcessingEvent {
    private final DomainEventStream message;
    private final EventProcessContext processContext;
    private ProcessingEventMailBox mailbox;

    public ProcessingEvent(DomainEventStream message, EventProcessContext processContext) {
        this.message = message;
        this.processContext = processContext;
    }

    public ProcessingEventMailBox getMailbox() {
        return mailbox;
    }

    public void setMailbox(ProcessingEventMailBox mailbox) {
        this.mailbox = mailbox;
    }

    public void complete() {
        processContext.notifyEventProcessed();
        if (mailbox != null) {
            mailbox.completeRun();
        }
    }

    public EventProcessContext getProcessContext() {
        return processContext;
    }

    public DomainEventStream getMessage() {
        return message;
    }
}
