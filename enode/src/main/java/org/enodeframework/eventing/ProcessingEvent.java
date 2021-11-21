package org.enodeframework.eventing;

/**
 * @author anruence@gmail.com
 */
public class ProcessingEvent {
    private final DomainEventStreamMessage message;
    private final IEventProcessContext processContext;
    private ProcessingEventMailBox mailbox;

    public ProcessingEvent(DomainEventStreamMessage message, IEventProcessContext processContext) {
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

    public IEventProcessContext getProcessContext() {
        return processContext;
    }

    public DomainEventStreamMessage getMessage() {
        return message;
    }
}
