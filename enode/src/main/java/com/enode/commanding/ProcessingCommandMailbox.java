package com.enode.commanding;

import com.enode.common.logging.ENodeLogger;
import com.enode.common.threading.ManualResetEvent;
import org.slf4j.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProcessingCommandMailbox {
    private static final Logger logger = ENodeLogger.getLog();

    private final Object lockObj = new Object();
    // TODO async lock
    private final Object lockObj2 = new Object();
    private final ConcurrentMap<Long, ProcessingCommand> messageDict;
    private final Map<Long, CommandResult> requestToCompleteCommandDict;
    private final IProcessingCommandHandler messageHandler;
    private final ManualResetEvent pauseWaitHandle;
    private final ManualResetEvent processingWaitHandle;
    private final int batchSize;
    private final String aggregateRootId;
    private final int commandMailBoxPersistenceMaxBatchSize = 1000;
    private long nextSequence;
    private long consumingSequence;
    private long consumedSequence;
    private AtomicBoolean isRunning;
    private volatile boolean isProcessingCommand;
    private volatile boolean isPaused;
    private Date lastActiveTime;

    public ProcessingCommandMailbox(String aggregaterootid, IProcessingCommandHandler messageHandler) {
        this.messageDict = new ConcurrentHashMap<>();
        this.requestToCompleteCommandDict = new HashMap<>();
        this.pauseWaitHandle = new ManualResetEvent(false);
        this.processingWaitHandle = new ManualResetEvent(false);
        this.batchSize = commandMailBoxPersistenceMaxBatchSize;
        this.aggregateRootId = aggregaterootid;
        this.messageHandler = messageHandler;
        this.consumedSequence = -1;
        this.isRunning = new AtomicBoolean(false);
        this.lastActiveTime = new Date();
    }

    public String getAggregateRootId() {
        return aggregateRootId;
    }

    public void enqueueMessage(ProcessingCommand message) {
        //TODO synchronized
        synchronized (lockObj) {
            message.setSequence(this.nextSequence);
            message.setMailbox(this);
            // If the specified key is not already associated with a value (or is mapped to null) associates it with the given value and returns null, else returns the current value.
            ProcessingCommand processingCommand = messageDict.putIfAbsent(message.getSequence(), message);
            if (processingCommand == null) {
                this.nextSequence++;
            }
        }
        this.lastActiveTime = new Date();
        tryRun();
    }

    public void pause() {
        this.lastActiveTime = new Date();
        this.pauseWaitHandle.reset();
        while (isProcessingCommand) {
            logger.info("Request to pause the command mailbox, but the mailbox is currently processing command, so we should wait for a while, aggregateRootId: {}", aggregateRootId);
            this.processingWaitHandle.waitOne(1000);
        }
        this.isPaused = true;
    }

    public void resume() {
        this.lastActiveTime = new Date();
        this.isPaused = false;
        this.pauseWaitHandle.set();
        tryRun();
    }

    public void resetConsumingSequence(long consumingSequence) {
        this.lastActiveTime = new Date();
        this.consumingSequence = consumingSequence;
        this.requestToCompleteCommandDict.clear();
    }

    //TODO async
    public CompletableFuture completeMessage(ProcessingCommand processingCommand, CommandResult commandResult) {
        //TODO synchronized
        synchronized (lockObj2) {
            lastActiveTime = new Date();
            try {
                if (processingCommand.getSequence() == consumedSequence + 1) {
                    messageDict.remove(processingCommand.getSequence());
                    completeCommand(processingCommand, commandResult);
                    consumedSequence = processNextCompletedCommands(processingCommand.getSequence());
                } else if (processingCommand.getSequence() > consumedSequence + 1) {
                    requestToCompleteCommandDict.put(processingCommand.getSequence(), commandResult);
                } else if (processingCommand.getSequence() < consumedSequence + 1) {
                    messageDict.remove(processingCommand.getSequence());
                    completeCommand(processingCommand, commandResult);
                    requestToCompleteCommandDict.remove(processingCommand.getSequence());
                }
            } catch (Exception ex) {
                logger.error(String.format("Command mailbox complete command failed, commandId: %s, aggregateRootId: %s", processingCommand.getMessage().id(), processingCommand.getMessage().getAggregateRootId()), ex);
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    public void run() {
        lastActiveTime = new Date();
        while (isPaused) {
            logger.info("Command mailbox is pausing and we should wait for a while, aggregateRootId: {}", aggregateRootId);
            pauseWaitHandle.waitOne(1000);
        }

        ProcessingCommand processingCommand = null;

        try {
            processingWaitHandle.reset();
            isProcessingCommand = true;
            int count = 0;

            while (consumingSequence < nextSequence && count < batchSize) {
                processingCommand = getProcessingCommand(consumingSequence);
                if (processingCommand != null) {
                    messageHandler.handle(processingCommand);
                }
                consumingSequence++;
                count++;
            }
        } catch (Throwable ex) {
            logger.error(String.format("Command mailbox run has unknown exception, aggregateRootId: %s, commandId: %s", aggregateRootId, processingCommand != null ? processingCommand.getMessage().id() : ""), ex);
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                //ignore
                logger.error("mailbox interrupted", e);
            }
        } finally {
            isProcessingCommand = false;
            processingWaitHandle.set();
            exit();
            if (consumingSequence < nextSequence) {
                tryRun();
            }
        }
    }

    public boolean isInactive(int timeoutSeconds) {
        return (System.currentTimeMillis() - lastActiveTime.getTime()) >= timeoutSeconds * 1000L;
    }

    private ProcessingCommand getProcessingCommand(long sequence) {
        return messageDict.get(sequence);
    }

    private long processNextCompletedCommands(long baseSequence) {
        long returnSequence = baseSequence;
        long nextSequence = baseSequence + 1;

        while (requestToCompleteCommandDict.containsKey(nextSequence)) {
            ProcessingCommand processingCommand = messageDict.remove(nextSequence);
            if (processingCommand != null) {
                CommandResult commandResult = requestToCompleteCommandDict.get(nextSequence);
                completeCommand(processingCommand, commandResult);
            }
            requestToCompleteCommandDict.remove(nextSequence);
            returnSequence = nextSequence;
            nextSequence++;
        }

        return returnSequence;
    }

    private CompletableFuture completeCommand(ProcessingCommand processingCommand, CommandResult commandResult) {
        try {
            return processingCommand.completeAsync(commandResult);
        } catch (Exception ex) {
            logger.error(String.format("Failed to complete command, commandId: %s, aggregateRootId: %s", processingCommand.getMessage().id(), processingCommand.getMessage().getAggregateRootId()), ex);
            return CompletableFuture.completedFuture(null);
        }
    }

    private void tryRun() {
        if (tryEnter()) {
            CompletableFuture.runAsync(this::run);
        }
    }

    private boolean tryEnter() {
        return isRunning.compareAndSet(false, true);
    }

    private void exit() {
        isRunning.getAndSet(false);
    }

    public Date getLastActiveTime() {
        return this.lastActiveTime;
    }

    public boolean isRunning() {
        return isRunning.get();
    }
}
