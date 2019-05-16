package com.enodeframework.queue.applicationmessage;

import com.enodeframework.common.serializing.IJsonSerializer;
import com.enodeframework.infrastructure.IApplicationMessage;
import com.enodeframework.infrastructure.IMessageProcessor;
import com.enodeframework.infrastructure.ITypeNameProvider;
import com.enodeframework.infrastructure.ProcessingApplicationMessage;
import com.enodeframework.infrastructure.impl.DefaultMessageProcessContext;
import com.enodeframework.queue.IMessageContext;
import com.enodeframework.queue.IMessageHandler;
import com.enodeframework.queue.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class ApplicationMessageListener implements IMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationMessageListener.class);

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
