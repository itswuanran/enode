package com.enodeframework.rocketmq.message;

import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.common.message.Message;
import com.enodeframework.commanding.CommandResult;
import com.enodeframework.commanding.CommandReturnType;
import com.enodeframework.commanding.ICommand;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.io.AsyncTaskStatus;
import com.enodeframework.common.io.Await;
import com.enodeframework.common.utilities.Ensure;
import com.enodeframework.queue.QueueMessage;
import com.enodeframework.queue.command.CommandService;

import java.util.concurrent.CompletableFuture;

public class RocketMQCommandService extends CommandService {

    private DefaultMQProducer defaultMQProducer;

    @Override
    public CompletableFuture<AsyncTaskResult> sendAsync(ICommand command) {
        try {
            QueueMessage queueMessage = buildCommandMessage(command, false);
            Message message = RocketMQTool.covertToProducerRecord(queueMessage);
            return SendRocketMQService.sendMessageAsync(defaultMQProducer, message, queueMessage.getRouteKey());
        } catch (Exception ex) {
            return CompletableFuture.completedFuture(new AsyncTaskResult<>(AsyncTaskStatus.Failed, ex.getMessage()));
        }
    }

    @Override
    public CompletableFuture<AsyncTaskResult<CommandResult>> executeAsync(ICommand command) {
        return executeAsync(command, CommandReturnType.CommandExecuted);
    }

    @Override
    public CompletableFuture<AsyncTaskResult<CommandResult>> executeAsync(ICommand command, CommandReturnType commandReturnType) {
        try {
            Ensure.notNull(commandResultProcessor, "commandResultProcessor");
            CompletableFuture<AsyncTaskResult<CommandResult>> taskCompletionSource = new CompletableFuture<>();
            commandResultProcessor.registerProcessingCommand(command, commandReturnType, taskCompletionSource);
            QueueMessage queueMessage = buildCommandMessage(command, true);
            Message message = RocketMQTool.covertToProducerRecord(queueMessage);
            CompletableFuture<AsyncTaskResult> sendMessageAsync = SendRocketMQService.sendMessageAsync(defaultMQProducer, message, queueMessage.getRouteKey());
            Await.get(sendMessageAsync);
            sendMessageAsync.thenAccept(sendResult -> {
                if (sendResult.getStatus().equals(AsyncTaskStatus.Success)) {
                    //_commandResultProcessor中会继续等命令或事件处理完成的状态
                    Await.get(taskCompletionSource);
                } else {
                    commandResultProcessor.processFailedSendingCommand(command);
                    taskCompletionSource.complete(new AsyncTaskResult<>(sendResult.getStatus(), sendResult.getErrorMessage()));
                }
            });
            return taskCompletionSource;
        } catch (Exception ex) {
            return CompletableFuture.completedFuture(new AsyncTaskResult<>(AsyncTaskStatus.Failed, ex.getMessage()));
        }
    }

    public DefaultMQProducer getDefaultMQProducer() {
        return defaultMQProducer;
    }

    public void setDefaultMQProducer(DefaultMQProducer defaultMQProducer) {
        this.defaultMQProducer = defaultMQProducer;
    }
}
