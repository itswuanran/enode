package org.enodeframework.samples.controller.bank;

import com.google.common.base.Stopwatch;
import org.enodeframework.commanding.CommandBus;
import org.enodeframework.commanding.CommandResult;
import org.enodeframework.commanding.CommandReturnType;
import org.enodeframework.common.io.Task;
import org.enodeframework.common.utils.IdGenerator;
import org.enodeframework.samples.commands.bank.CreateAccountCommand;
import org.enodeframework.samples.commands.bank.StartDepositTransactionCommand;
import org.enodeframework.samples.commands.bank.StartTransferTransactionCommand;
import org.enodeframework.samples.domain.bank.transfertransaction.TransferTransactionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

@RestController
@RequestMapping("/bank")
public class BankController {
    @Autowired
    private CommandBus commandBus;

    @RequestMapping("transfer")
    public Mono<Boolean> transfer() {
        String account1 = IdGenerator.id();
        String account2 = IdGenerator.id();
        String account3 = "INVALID-" + IdGenerator.id();
        //创建两个银行账户
        //每个账户都存入1000元，这里要等到事件执行完成才算是存入成功，否则有可能在下面操作转账记录聚合根时，出现余额为0的情况
        CompletableFuture<CommandResult> future1 = commandBus.executeAsync(new CreateAccountCommand(account1, "account1"), CommandReturnType.EventHandled).thenCompose(x -> {
            return (commandBus.executeAsync(new StartDepositTransactionCommand(IdGenerator.id(), account1, 1000), CommandReturnType.EventHandled));
        });
        CompletableFuture<CommandResult> future2 = commandBus.executeAsync(new CreateAccountCommand(account2, "account2"), CommandReturnType.EventHandled).thenCompose(x -> {
            return (commandBus.executeAsync(new StartDepositTransactionCommand(IdGenerator.id(), account2, 1000), CommandReturnType.EventHandled));
        });
        CompletableFuture<Boolean> future = CompletableFuture.allOf(future1, future2).thenCompose(x -> {
            //账户1向账户3转账300元，交易会失败，因为账户3不存在
            return commandBus.executeAsync(new StartTransferTransactionCommand(IdGenerator.id(), new TransferTransactionInfo(account1, account3, 300D)))
                .thenCompose(y -> {
                    //账户1向账户2转账1200元，交易会失败，因为余额不足
                    return commandBus.sendAsync(new StartTransferTransactionCommand(IdGenerator.id(), new TransferTransactionInfo(account1, account2, 1200D)));
                }).thenCompose(z -> {
                    //账户2向账户1转账500元，交易成功
                    return commandBus.sendAsync(new StartTransferTransactionCommand(IdGenerator.id(), new TransferTransactionInfo(account2, account1, 500D)));
                });
        });
        return Mono.fromFuture(future);
    }

    @RequestMapping("perf")
    public Mono<Object> perf(@RequestParam("count") int accountCount) {
        List<String> accountList = new ArrayList<>();
        int transactionCount = 1000;
        double depositAmount = 1000000000D;
        double transferAmount = 100D;
        //创建银行账户
        for (int i = 0; i < accountCount; i++) {
            String accountId = IdGenerator.id();
            commandBus.executeAsync(new CreateAccountCommand(accountId, "SampleAccount" + i), CommandReturnType.EventHandled).join();
            accountList.add(accountId);
        }
        //每个账户都存入初始额度
        for (String accountId : accountList) {
            commandBus.sendAsync(new StartDepositTransactionCommand(IdGenerator.id(), accountId, depositAmount)).join();
        }
        CountDownLatch countDownLatch = new CountDownLatch(transactionCount);
        Stopwatch watch = Stopwatch.createStarted();
        for (int i = 0; i < transactionCount; i++) {
            int sourceAccountIndex = new Random().nextInt(accountCount - 1);
            int targetAccountIndex = sourceAccountIndex + 1;
            String sourceAccount = accountList.get(sourceAccountIndex);
            String targetAccount = accountList.get(targetAccountIndex);
            commandBus.executeAsync(new StartTransferTransactionCommand(IdGenerator.id(), new TransferTransactionInfo(sourceAccount, targetAccount, transferAmount)), CommandReturnType.EventHandled)
                .whenComplete((x, y) -> {
                    countDownLatch.countDown();
                });
        }
        Task.await(countDownLatch);
        watch.stop();
        return Mono.justOrEmpty(watch.elapsed());
    }
}
