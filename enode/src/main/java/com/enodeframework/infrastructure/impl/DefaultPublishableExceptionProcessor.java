package com.enodeframework.infrastructure.impl;

import com.enodeframework.infrastructure.IPublishableException;
import com.enodeframework.infrastructure.ProcessingPublishableExceptionMessage;

public class DefaultPublishableExceptionProcessor extends DefaultMessageProcessor<ProcessingPublishableExceptionMessage, IPublishableException> {

    @Override
    public String getMessageName() {
        return "exception message";
    }
}

