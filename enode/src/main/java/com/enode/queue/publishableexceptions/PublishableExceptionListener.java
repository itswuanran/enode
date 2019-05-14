package com.enode.queue.publishableexceptions;

import com.enode.common.logging.ENodeLogger;
import com.enode.common.serializing.IJsonSerializer;
import com.enode.infrastructure.IMessageProcessor;
import com.enode.infrastructure.IPublishableException;
import com.enode.infrastructure.ISequenceMessage;
import com.enode.infrastructure.ITypeNameProvider;
import com.enode.infrastructure.ProcessingPublishableExceptionMessage;
import com.enode.infrastructure.WrappedRuntimeException;
import com.enode.infrastructure.impl.DefaultMessageProcessContext;
import com.enode.queue.IMessageContext;
import com.enode.queue.IMessageHandler;
import com.enode.queue.QueueMessage;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class PublishableExceptionListener implements IMessageHandler {

    private static final Logger logger = ENodeLogger.getLog();

    @Autowired
    protected IJsonSerializer jsonSerializer;

    @Autowired
    protected ITypeNameProvider typeNameProvider;

    @Autowired
    protected IMessageProcessor<ProcessingPublishableExceptionMessage, IPublishableException> publishableExceptionProcessor;

    @Override
    public void handle(QueueMessage queueMessage, IMessageContext context) {
        PublishableExceptionMessage exceptionMessage = jsonSerializer.deserialize(queueMessage.getBody(), PublishableExceptionMessage.class);
        Class exceptionType = typeNameProvider.getType(exceptionMessage.getExceptionType());
        IPublishableException exception;
        try {
            exception = (IPublishableException) exceptionType.getConstructor().newInstance();
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
        exception.setId(exceptionMessage.getUniqueId());
        exception.setTimestamp(exceptionMessage.getTimestamp());
        exception.restoreFrom(exceptionMessage.getSerializableInfo());

        if (exception instanceof ISequenceMessage) {
            ISequenceMessage sequenceMessage = (ISequenceMessage) exception;
            sequenceMessage.setAggregateRootTypeName(exceptionMessage.getAggregateRootTypeName());
            sequenceMessage.setAggregateRootStringId(exceptionMessage.getAggregateRootId());
        }

        DefaultMessageProcessContext processContext = new DefaultMessageProcessContext(queueMessage, context);
        ProcessingPublishableExceptionMessage processingMessage = new ProcessingPublishableExceptionMessage(exception, processContext);
        logger.info("ENode exception message received, messageId: {}, aggregateRootId: {}, aggregateRootType: {}", exceptionMessage.getUniqueId(), exceptionMessage.getAggregateRootId(), exceptionMessage.getAggregateRootTypeName());
        publishableExceptionProcessor.process(processingMessage);
    }
}
