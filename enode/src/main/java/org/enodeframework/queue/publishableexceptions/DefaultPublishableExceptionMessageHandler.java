package org.enodeframework.queue.publishableexceptions;

import org.enodeframework.common.exception.MessageInstanceCreateException;
import org.enodeframework.common.serializing.SerializeService;
import org.enodeframework.domain.DomainExceptionMessage;
import org.enodeframework.infrastructure.TypeNameProvider;
import org.enodeframework.messaging.MessageDispatcher;
import org.enodeframework.queue.MessageContext;
import org.enodeframework.queue.MessageHandler;
import org.enodeframework.queue.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultPublishableExceptionMessageHandler implements MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(DefaultPublishableExceptionMessageHandler.class);

    private final TypeNameProvider typeNameProvider;

    private final MessageDispatcher messageDispatcher;

    private final SerializeService serializeService;

    public DefaultPublishableExceptionMessageHandler(TypeNameProvider typeNameProvider, MessageDispatcher messageDispatcher, SerializeService serializeService) {
        this.typeNameProvider = typeNameProvider;
        this.messageDispatcher = messageDispatcher;
        this.serializeService = serializeService;
    }

    @Override
    public void handle(QueueMessage queueMessage, MessageContext context) {
        logger.info("Received domain exception message: {}", queueMessage);
        GenericPublishableExceptionMessage exceptionMessage = serializeService.deserialize(queueMessage.getBody(), GenericPublishableExceptionMessage.class);
        Class<?> exceptionType = typeNameProvider.getType(exceptionMessage.getExceptionType());
        DomainExceptionMessage exception;
        try {
            exception = (DomainExceptionMessage) exceptionType.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new MessageInstanceCreateException(e);
        }
        exception.setId(exceptionMessage.getUniqueId());
        exception.setTimestamp(exceptionMessage.getTimestamp());
        exception.setItems(exceptionMessage.getItems());
        exception.restoreFrom(exceptionMessage.getSerializableInfo());
        messageDispatcher.dispatchMessageAsync(exception).whenComplete((x, y) -> {
            context.onMessageHandled(queueMessage);
        });
    }
}
