package com.enode.infrastructure.impl;

import com.enode.common.logging.ENodeLogger;
import com.enode.common.scheduling.IScheduleService;
import com.enode.infrastructure.IMessage;
import com.enode.infrastructure.IMessageProcessor;
import com.enode.infrastructure.IProcessingMessage;
import com.enode.infrastructure.IProcessingMessageHandler;
import com.enode.infrastructure.IProcessingMessageScheduler;
import com.enode.infrastructure.ProcessingMessageMailbox;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class DefaultMessageProcessor<X extends IProcessingMessage<X, Y>, Y extends IMessage> implements IMessageProcessor<X, Y> {

    private static final Logger logger = ENodeLogger.getLog();

    // AggregateRootMaxInactiveSeconds = 3600 * 24 * 3;

    private final int timeoutSeconds = 3600 * 24 * 3;

    private final int scanExpiredAggregateIntervalMilliseconds = 5000;

    private final String taskName;

    private ConcurrentMap<String, ProcessingMessageMailbox<X, Y>> mailboxDict;

    @Autowired
    private IScheduleService scheduleService;

    @Autowired
    private IProcessingMessageScheduler<X, Y> processingMessageScheduler;

    @Autowired
    private IProcessingMessageHandler<X, Y> processingMessageHandler;

    public DefaultMessageProcessor() {
        mailboxDict = new ConcurrentHashMap<>();
        taskName = "CleanInactiveAggregates" + System.nanoTime() + new Random().nextInt(10000);
    }

    public String getMessageName() {
        return "message";
    }

    @Override
    public void process(X processingMessage) {
        String routingKey = processingMessage.getMessage().getRoutingKey();
        if (routingKey != null && !"".equals(routingKey.trim())) {
            ProcessingMessageMailbox<X, Y> mailbox = mailboxDict.computeIfAbsent(routingKey, key -> new ProcessingMessageMailbox<>(routingKey, processingMessageScheduler, processingMessageHandler));
            mailbox.enqueueMessage(processingMessage);
        } else {
            processingMessageScheduler.scheduleMessage(processingMessage);
        }
    }

    @Override
    public void start() {
        scheduleService.startTask(taskName, this::cleanInactiveMailbox, scanExpiredAggregateIntervalMilliseconds, scanExpiredAggregateIntervalMilliseconds);
    }

    @Override
    public void stop() {
        scheduleService.stopTask(taskName);
    }

    private void cleanInactiveMailbox() {
        List<Map.Entry<String, ProcessingMessageMailbox<X, Y>>> inactiveList = mailboxDict.entrySet().stream().filter(entry ->
                entry.getValue().isInactive(timeoutSeconds) && !entry.getValue().isRunning()
        ).collect(Collectors.toList());

        inactiveList.forEach(entry -> {
            if (mailboxDict.remove(entry.getKey()) != null) {
                logger.info("Removed inactive {} mailbox, aggregateRootId: {}", getMessageName(), entry.getKey());
            }
        });
    }
}
