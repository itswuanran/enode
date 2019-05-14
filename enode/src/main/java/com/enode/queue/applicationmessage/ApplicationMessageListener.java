package com.enode.queue.applicationmessage;

import com.enode.common.logging.ENodeLogger;
import com.enode.common.serializing.IJsonSerializer;
import com.enode.infrastructure.IApplicationMessage;
import com.enode.infrastructure.IMessageProcessor;
import com.enode.infrastructure.ITypeNameProvider;
import com.enode.infrastructure.ProcessingApplicationMessage;
import com.enode.infrastructure.impl.DefaultMessageProcessContext;
import com.enode.queue.IMessageContext;
import com.enode.queue.IMessageHandler;
import com.enode.queue.QueueMessage;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class ApplicationMessageListener implements IMessageHandler {

    private static final Logger logger = ENodeLogger.getLog();

    @Autowired
    protected IJsonSerializer jsonSerializer;

    @Autowired
    protected ITypeNameProvider typeNameProvider;

    @Autowired
    protected IMessageProcessor<ProcessingApplicationMessage, IApplicationMessage> processor;

    @Override
    public void handle(QueueMessage queueMessage, IMessageContext context) {
        String msg = queueMessage.getBody();
        ApplicationDataMessage appDataMessage = jsonSerializer.deserialize(msg, ApplicationDataMessage.class);
        Class applicationMessageType;

        try {
            applicationMessageType = typeNameProvider.getType(appDataMessage.getApplicationMessageType());
        } catch (Exception e) {
            logger.warn("Consume application message exception:", e);
            return;
        }
        IApplicationMessage message = (IApplicationMessage) jsonSerializer.deserialize(appDataMessage.getApplicationMessageData(), applicationMessageType);
        DefaultMessageProcessContext processContext = new DefaultMessageProcessContext(queueMessage, context);
        ProcessingApplicationMessage processingMessage = new ProcessingApplicationMessage(message, processContext);
        logger.info("ENode application message received, messageId: {}, routingKey: {}", message.id(), message.getRoutingKey());
        processor.process(processingMessage);
    }
}
