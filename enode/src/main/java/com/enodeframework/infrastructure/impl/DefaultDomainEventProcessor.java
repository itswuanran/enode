package com.enodeframework.infrastructure.impl;

import com.enodeframework.eventing.DomainEventStreamMessage;
import com.enodeframework.infrastructure.ProcessingDomainEventStreamMessage;

public class DefaultDomainEventProcessor extends DefaultMessageProcessor<ProcessingDomainEventStreamMessage, DomainEventStreamMessage> {

    @Override
    public String getMessageName() {
        return "event message";
    }
}
