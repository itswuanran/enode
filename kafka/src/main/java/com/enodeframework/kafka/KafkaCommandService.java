package com.enodeframework.kafka;

import com.enodeframework.commanding.CommandResult;
import com.enodeframework.commanding.CommandReturnType;
import com.enodeframework.commanding.ICommand;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.io.AsyncTaskStatus;
import com.enodeframework.common.utilities.Ensure;
import com.enodeframework.queue.QueueMessage;
import com.enodeframework.queue.command.AbstractCommandService;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

public class KafkaCommandService extends AbstractCommandService {

    private KafkaProducer<String, String> producer;
    @Autowired
    private SendMessageService sendMessageService;

    public KafkaProducer<String, String> getProducer() {
        return producer;
    }

    public void setProducer(KafkaProducer<String, String> producer) {
        this.producer = producer;
    }

    @Override
    public CompletableFuture<AsyncTaskResult> sendAsync(ICommand command) {
        try {
            return sendMessageService.sendMessageAsync(producer, buildKafkaMessage(command, false));
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

            CompletableFuture<AsyncTaskResult> sendMessageAsync = sendMessageService.sendMessageAsync(producer, buildKafkaMessage(command, true));
            sendMessageAsync.thenAccept(sendResult -> {
                if (sendResult.getStatus().equals(AsyncTaskStatus.Success)) {
                    //commandResultProcessor中会继续等命令或事件处理完成的状态
                } else {
                    //TODO 是否删除下面一行代码
                    taskCompletionSource.complete(new AsyncTaskResult<>(sendResult.getStatus(), sendResult.getErrorMessage()));
                    commandResultProcessor.processFailedSendingCommand(command);
                }
            });

            return taskCompletionSource;
        } catch (Exception ex) {
            return CompletableFuture.completedFuture(new AsyncTaskResult<>(AsyncTaskStatus.Failed, ex.getMessage()));
        }
    }


    protected ProducerRecord<String, String> buildKafkaMessage(ICommand command, boolean needReply) {
        QueueMessage queueMessage = buildCommandMessage(command, needReply);
        return KafkaTool.covertToProducerRecord(queueMessage);
    }

}
