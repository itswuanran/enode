package org.enodeframework.queue.command;

import com.google.common.base.Strings;
import org.enodeframework.commanding.CommandMessage;
import org.enodeframework.commanding.CommandProcessor;
import org.enodeframework.commanding.ProcessingCommand;
import org.enodeframework.commanding.impl.DefaultCommandExecuteContext;
import org.enodeframework.common.serializing.SerializeService;
import org.enodeframework.configurations.SysProperties;
import org.enodeframework.domain.AggregateStorage;
import org.enodeframework.domain.Repository;
import org.enodeframework.infrastructure.TypeNameProvider;
import org.enodeframework.queue.MessageContext;
import org.enodeframework.queue.MessageHandler;
import org.enodeframework.queue.QueueMessage;
import org.enodeframework.queue.SendReplyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class DefaultCommandMessageHandler implements MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(DefaultCommandMessageHandler.class);

    private final SendReplyService sendReplyService;

    private final TypeNameProvider typeNameProvider;

    private final CommandProcessor commandProcessor;

    private final Repository repository;

    private final AggregateStorage aggregateRootStorage;

    private final SerializeService serializeService;

    public DefaultCommandMessageHandler(SendReplyService sendReplyService, TypeNameProvider typeNameProvider, CommandProcessor commandProcessor, Repository repository, AggregateStorage aggregateRootStorage, SerializeService serializeService) {
        this.sendReplyService = sendReplyService;
        this.typeNameProvider = typeNameProvider;
        this.commandProcessor = commandProcessor;
        this.repository = repository;
        this.aggregateRootStorage = aggregateRootStorage;
        this.serializeService = serializeService;
    }

    @Override
    public void handle(QueueMessage queueMessage, MessageContext context) {
        logger.info("Received command message: {}", serializeService.serialize(queueMessage));
        GenericCommandMessage commandMessage = serializeService.deserialize(queueMessage.getBody(), GenericCommandMessage.class);
        Class<?> commandType = typeNameProvider.getType(commandMessage.getCommandType());
        CommandMessage<?> command = (CommandMessage<?>) serializeService.deserialize(commandMessage.getCommandData(), commandType);
        DefaultCommandExecuteContext commandExecuteContext = new DefaultCommandExecuteContext(repository, aggregateRootStorage, queueMessage, context, commandMessage, sendReplyService);
        Map<String, Object> commandItems = new HashMap<>();
        String uri = commandMessage.getReplyAddress();
        if (!Strings.isNullOrEmpty(uri)) {
            commandItems.put(SysProperties.ITEMS_COMMAND_REPLY_ADDRESS_KEY, uri);
        }
        commandProcessor.process(new ProcessingCommand(command, commandExecuteContext, commandItems));
    }
}
