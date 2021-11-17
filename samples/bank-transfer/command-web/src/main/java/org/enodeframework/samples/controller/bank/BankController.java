package org.enodeframework.samples.controller.bank;

import com.google.common.base.Stopwatch;
import org.enodeframework.commanding.CommandReturnType;
import org.enodeframework.commanding.ICommandService;
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
import java.util.concurrent.CountDownLatch;

@RestController
@RequestMapping("/bank")
public class BankController {
    @Autowired
    private ICommandService commandService;

    @RequestMapping("deposit")
    public Mono deposit() {
        String account1 = IdGenerator.nextId();
        String account2 = IdGenerator.nextId();
        String account3 = "INVALID-" + IdGenerator.nextId();
        //创建两个银行账户
        Task.await(commandService.executeAsync(new CreateAccountCommand(account1, "雪华"), CommandReturnType.EventHandled));
        Task.await(commandService.executeAsync(new CreateAccountCommand(account2, "凯锋"), CommandReturnType.EventHandled));
        //每个账户都存入1000元，这里要等到事件执行完成才算是存入成功，否则有可能在下面操作转账记录聚合根时，出现余额为0的情况
        Task.await(commandService.executeAsync(new StartDepositTransactionCommand(IdGenerator.nextId(), account1, 1000), CommandReturnType.EventHandled));
        Task.await(commandService.executeAsync(new StartDepositTransactionCommand(IdGenerator.nextId(), account2, 1000), CommandReturnType.EventHandled));

        //账户1向账户3转账300元，交易会失败，因为账户3不存在
        Task.await(commandService.executeAsync(new StartTransferTransactionCommand(IdGenerator.nextId(), new TransferTransactionInfo(account1, account3, 300D))));
        //账户1向账户2转账1200元，交易会失败，因为余额不足
        Task.await(commandService.sendAsync(new StartTransferTransactionCommand(IdGenerator.nextId(), new TransferTransactionInfo(account1, account2, 1200D))));
        //账户2向账户1转账500元，交易成功
        Task.await(commandService.sendAsync(new StartTransferTransactionCommand(IdGenerator.nextId(), new TransferTransactionInfo(account2, account1, 500D))));
        return Mono.justOrEmpty("success");
    }

    @RequestMapping("perf")
    public Mono perf(@RequestParam("count") int accountCount) {
        List<String> accountList = new ArrayList<>();
        int transactionCount = 1000;
        double depositAmount = 1000000000D;
        double transferAmount = 100D;
        //创建银行账户
        for (int i = 0; i < accountCount; i++) {
            String accountId = IdGenerator.nextId();
            commandService.executeAsync(new CreateAccountCommand(accountId, "SampleAccount" + i), CommandReturnType.EventHandled).join();
            accountList.add(accountId);
        }
        //每个账户都存入初始额度
        for (String accountId : accountList) {
            commandService.sendAsync(new StartDepositTransactionCommand(IdGenerator.nextId(), accountId, depositAmount)).join();
        }
        CountDownLatch countDownLatch = new CountDownLatch(transactionCount);
        Stopwatch watch = Stopwatch.createStarted();
        for (int i = 0; i < transactionCount; i++) {
            int sourceAccountIndex = new Random().nextInt(accountCount - 1);
            int targetAccountIndex = sourceAccountIndex + 1;
            String sourceAccount = accountList.get(sourceAccountIndex);
            String targetAccount = accountList.get(targetAccountIndex);
            commandService.executeAsync(new StartTransferTransactionCommand(IdGenerator.nextId(), new TransferTransactionInfo(sourceAccount, targetAccount, transferAmount)), CommandReturnType.EventHandled)
                .whenComplete((x, y) -> {
                    countDownLatch.countDown();
                });
        }
        Task.await(countDownLatch);
        watch.stop();
        return Mono.justOrEmpty(watch.elapsed());
    }
}
