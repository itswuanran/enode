package org.enodeframework.queue.applicationmessage;

import org.enodeframework.common.serializing.SerializeService;
import org.enodeframework.infrastructure.TypeNameProvider;
import org.enodeframework.messaging.ApplicationMessage;
import org.enodeframework.messaging.MessageDispatcher;
import org.enodeframework.queue.MessageContext;
import org.enodeframework.queue.MessageHandler;
import org.enodeframework.queue.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultApplicationMessageHandler implements MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(DefaultApplicationMessageHandler.class);
    private final TypeNameProvider typeNameProvider;
    private final MessageDispatcher messageDispatcher;
    private final SerializeService serializeService;

    public DefaultApplicationMessageHandler(TypeNameProvider typeNameProvider, MessageDispatcher messageDispatcher, SerializeService serializeService) {
        this.typeNameProvider = typeNameProvider;
        this.messageDispatcher = messageDispatcher;
        this.serializeService = serializeService;
    }

    @Override
    public void handle(QueueMessage queueMessage, MessageContext context) {
        logger.info("Received application message: {}", serializeService.serialize(queueMessage));
        String msg = queueMessage.getBody();
        GenericApplicationMessage appDataMessage = serializeService.deserialize(msg, GenericApplicationMessage.class);
        Class<?> applicationMessageType = typeNameProvider.getType(appDataMessage.getApplicationMessageType());
        ApplicationMessage message = (ApplicationMessage) serializeService.deserialize(appDataMessage.getApplicationMessageData(), applicationMessageType);
        messageDispatcher.dispatchMessageAsync(message).whenComplete((x, y) -> {
            context.onMessageHandled(queueMessage);
        });
    }
}
