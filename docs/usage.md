## 一些规约


### 为什么采用异步单一长连接?

因为服务的现状大都是服务提供者少，通常只有几台机器，而服务的消费者多，可能整个网站都在访问该服务。
在我们的这个场景里面，`command-web`只需要很少的机器就能满足前端大量的请求，`command-consumer`和`event-consumer`的机器相对较多些。
如果采用常规的“单请求单连接”的方式，服务提供者很容易就被压跨，通过单一连接，保证单一消费者不会压死提供者，长连接，减少连接握手验证等，并使用异步`IO`
，复用线程池，防止`C10K`问题。

### `CommandHandler` 中的逻辑约束

- `CommandHandler`是为了操作内存中的聚合根的，所以不会有异步操作，但后来`CommandHandler`的`Handle`
  方法也设计为了`handleAsync`了，目的是为了异步到底，否则异步链路中断的话，异步就没效果了
- `CommandAsyncHandler`是为了让开发者调用外部系统的接口的，也就是访问外部`IO`，所以用了`Async

> `CommandHandler`，`CommandAsyncHandler`这两个接口是用于不同的业务场景，`CommandHandler.handleAsync`
> 方法执行完成后，框架要从`context`中获取当前修改的聚合根的领域事件，然后去提交。而`CommandAsyncHandler.handleAsync`
> 方法执行完成后，不会有这个逻辑，而是看一下`handleAsync`方法执行的异步消息结果是什么，也就是`IApplicationMessage`。
> 目前已经删除了`CommandAsyncHandler`，统一使用`CommandHandler`来处理，异步结果会放在`context`中，通过访问 `#setResult`设置

### `CommandBus` `sendAsync` 和 `executeAsync`的区别

`sendAsync`只关注发送消息的结果
`executeAsync`发送消息的同时，关注命令的执行结果，返回的时机如下：

- `CommandReturnType.CommandExecuted`：`Command`执行完成，`Event`发布成功后返回结果
- `CommandReturnType.EventHandled`：`Event`处理完成后才返回结果

### `event`使用哪个订阅者发送处理结果

`event`的订阅者可能有很多个，所以`enode`
只要求有一个订阅者处理完事件后发送结果给发送命令的人即可，通过`defaultDomainEventMessageHandler`
中`sendEventHandledMessage`参数来设置是否发送，最终来决定由哪个订阅者来发送命令处理结果。

### application和exception消息的topic不见了
消息队列经过几次重构，把`application`和`exception`消息全部整合到`domain event`队列中去了，减少消息接入的复杂度


## 基础软件配置

### 消息队列配置
目前支持三种

#### `Pulsar`

```bash 
bin/pulsar standalone

```

#### `Kafka`

https://kafka.apache.org/quickstart

```bash
bin/zookeeper-server-start.sh config/zookeeper.properties
bin/kafka-server-start.sh config/server.properties
```

#### `RocketMQ`

https://rocketmq.apache.org/docs/quick-start/

启动`RocketMQ`服务：

```bash
nohup sh bin/mqnamesrv &
nohup sh bin/mqbroker -n 127.0.0.1:9876 &
```


## 数据库依赖

#### 表的含义

`event_stream` 表中存储的是每个聚合根和对应版本的领域事件历史记录
`published_version` 表中存储的每个聚合根当前的消费进度（版本）

注意有两个唯一索引，这个是实现幂等的常用思路，因为我们认为大部分情况下不会出现重复写问题

#### `MySQL`

```sql
CREATE TABLE event_stream (
  id BIGINT AUTO_INCREMENT NOT NULL,
  aggregate_root_type_name VARCHAR(256) NOT NULL,
  aggregate_root_id VARCHAR(64) NOT NULL,
  version INT NOT NULL,
  command_id VARCHAR(64) NOT NULL,
  events MEDIUMTEXT NOT NULL,
  create_at BIGINT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_aggregate_root_id_version (aggregate_root_id, version),
  UNIQUE KEY uk_aggregate_root_id_command_id (aggregate_root_id, command_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE published_version (
  id BIGINT AUTO_INCREMENT NOT NULL,
  processor_name VARCHAR(128) NOT NULL,
  aggregate_root_type_name VARCHAR(256) NOT NULL,
  aggregate_root_id VARCHAR(64) NOT NULL,
  version INT NOT NULL,
  create_at BIGINT NOT NULL,
  update_at BIGINT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_aggregate_root_id_version_processor_name (aggregate_root_id, version, processor_name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

```

#### `postgresql`

```sql
CREATE TABLE event_stream (
  id bigserial,
  aggregate_root_type_name varchar(256),
  aggregate_root_id varchar(64),
  version integer,
  command_id varchar(64),
  events text,
  create_at bigint,
  PRIMARY KEY (id),
  CONSTRAINT uk_aggregate_root_id_version UNIQUE (aggregate_root_id, version),
  CONSTRAINT uk_aggregate_root_id_command_id UNIQUE (aggregate_root_id, command_id)
);

CREATE TABLE published_version (
  id bigserial,
  processor_name varchar(128),
  aggregate_root_type_name varchar(256),
  aggregate_root_id varchar(64),
  version integer,
  create_at bigint,
  update_at bigint,
  PRIMARY KEY (id),
  CONSTRAINT uk_aggregate_root_id_version_processor_name UNIQUE (aggregate_root_id, version, processor_name)
);

```

#### `MongoDB`

```bash
db.event_stream.createIndex({aggregateRootId:1,commandId:1},{unique:true})
db.event_stream.createIndex({aggregateRootId:1,version:1},{unique:true})
db.published_version.createIndex({aggregateRootId:1,version:1,processorName:1,},{unique:true})
```


### 应用启动配置

新增`@EnableEnode`注解，可自动配置`Bean`，简化了接入方式。

### `enode`启动配置

```
@SpringBootApplication
@EnableEnode(value = "org.enodeframework.tests")
@ComponentScan(value = "org.enodeframework.tests")
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
```

### `Spring Boot`启动配置文件
依赖的系统配置属性

```properties
# enode eventstore (memory,mysql,pg,mongo,jdbc-mysql,jdbc-pg)
spring.enode.eventstore=mysql
# enode message queue (kafka,rocketmq,ons,pulsar,amqp)
spring.enode.mq=kafka
spring.enode.mq.topic.command=EnodeBankCommandTopic
spring.enode.mq.topic.event=EnodeBankEventTopic

# enode reply queue typo (tcp,redis,kafka,rocketmq,ons,pulsar,amqp)
spring.enode.reply=tcp
spring.enode.reply.topic=EnodeBankReplyTopic
```

### `kafka bean`配置

> 如果把生成者和消费者配置在一个config文件中，这里会产生存在一个循环依赖，为了避免这种情况，建议分开两个文件配置

#### producer

```
@Bean
public ProducerFactory<String, String> producerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.KAFKA_SERVER);
    props.put(ProducerConfig.RETRIES_CONFIG, 1);
    props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
    props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
    props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 1024000);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    return new DefaultKafkaProducerFactory<>(props);
}

@Bean
public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
    return new KafkaTemplate<>(producerFactory);
}
```

#### consumer

```
@Value("${spring.enode.mq.topic.command}")
private String commandTopic;

@Value("${spring.enode.mq.topic.event}")
private String eventTopic;

@Bean
public ConsumerFactory<String, String> consumerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.KAFKA_SERVER);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, Constants.DEFAULT_PRODUCER_GROUP);
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
    props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
    props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    return new DefaultKafkaConsumerFactory<>(props);
}

@Bean
public KafkaMessageListenerContainer<String, String> commandListenerContainer(KafkaMessageListener commandListener, ConsumerFactory<String, String> consumerFactory) {
    ContainerProperties properties = new ContainerProperties(commandTopic);
    properties.setGroupId(Constants.DEFAULT_CONSUMER_GROUP);
    properties.setMessageListener(commandListener);
    properties.setMissingTopicsFatal(false);
    return new KafkaMessageListenerContainer<>(consumerFactory, properties);
}

@Bean
public KafkaMessageListenerContainer<String, String> domainEventListenerContainer(KafkaMessageListener domainEventListener, ConsumerFactory<String, String> consumerFactory) {
    ContainerProperties properties = new ContainerProperties(eventTopic);
    properties.setGroupId(Constants.DEFAULT_PRODUCER_GROUP);
    properties.setMessageListener(domainEventListener);
    properties.setMissingTopicsFatal(false);
    properties.setAckMode(ContainerProperties.AckMode.MANUAL);
    return new KafkaMessageListenerContainer<>(consumerFactory, properties);
}
```

### `eventstore`数据源配置，目前支持(`MySQL` `MongoDB` `PostgreSQL` ...）

```java
public class DbConfig {
    @Bean("enodeMongoClient")
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "mongo")
    public MongoClient mongoClient(Vertx vertx) {
        return MongoClient.create(vertx, new JsonObject().put("db_name", "test"));
    }

    @Bean("enodeMySQLPool")
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "mysql")
    public MySQLPool enodeMySQLPool() {
        MySQLConnectOptions connectOptions = MySQLConnectOptions.fromUri(jdbcUrl.replaceAll("jdbc:", ""))
            .setUser(username)
            .setPassword(password);
        PoolOptions poolOptions = new PoolOptions()
            .setMaxSize(5);
        return MySQLPool.pool(connectOptions, poolOptions);
    }

    @Bean("enodePgPool")
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "pg")
    public PgPool pgPool() {
        PgConnectOptions connectOptions = PgConnectOptions.fromUri(pgJdbcUrl.replaceAll("jdbc:", ""))
            .setUser(pgUsername)
            .setPassword(pgPassword);
        PoolOptions poolOptions = new PoolOptions()
            .setMaxSize(5);
        return PgPool.pool(connectOptions, poolOptions);
    }

    @Bean("enodeMySQLDataSource")
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "jdbc-mysql")
    public DataSource enodeMySQLDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(com.mysql.cj.jdbc.Driver.class.getName());
        return dataSource;
    }

    @Bean("enodePgDataSource")
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "jdbc-pg")
    public DataSource enodePgDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(pgJdbcUrl);
        dataSource.setUsername(pgUsername);
        dataSource.setPassword(pgPassword);
        dataSource.setDriverClassName(org.postgresql.Driver.class.getName());
        return dataSource;
    }
}
```

```kotlin
@Command
class ChangeNoteTitleCommandHandler {
    @Subscribe
    suspend fun handleAsync(context: CommandContext, command: ChangeNoteTitleCommand) {
        val note = context.get(command.getAggregateRootId(), true, Note::class.java)
        note.changeTitle(command.title)
    }
}
```

```
@Command
public class BankAccountCommandHandler {
    @Subscribe
    public CompletableFuture<BankAccount> handleAsync(CommandContext context, AddTransactionPreparationCommand command) {
        CompletableFuture<BankAccount> future = context.getAsync(command.getAggregateRootId(), BankAccount.class);
        future.thenAccept(bankAccount -> {
            bankAccount.addTransactionPreparation(command.transactionId, command.transactionType, command.preparationType, command.amount);
        });
        return future;
    }
}
```

发送命令消息：

```java
CompletableFuture<CommandResult> future = commandService.executeAsync(createNoteCommand, CommandReturnType.EventHandled);
```

命令处理：

```
/**
 * 银行账户相关命令处理
 * CommandHandler<CreateAccountCommand>,                       //开户
 * CommandAsyncHandler<ValidateAccountCommand>,                //验证账户是否合法
 * CommandHandler<AddTransactionPreparationCommand>,           //添加预操作
 * CommandHandler<CommitTransactionPreparationCommand>         //提交预操作
 */
@Command
public class BankAccountCommandHandler {
    /**
     * 开户
     */
    @Subscribe
    public void handleAsync(CommandContext context, CreateAccountCommand command) {
        context.addAsync(new BankAccount(command.getAggregateRootId(), command.owner));
    }

    /**
     * 添加预操作
     */
    @Subscribe
    public CompletableFuture<BankAccount> handleAsync(CommandContext context, AddTransactionPreparationCommand command) {
        CompletableFuture<BankAccount> future = context.getAsync(command.getAggregateRootId(), BankAccount.class);
        future.thenAccept(bankAccount -> {
            bankAccount.addTransactionPreparation(command.transactionId, command.transactionType, command.preparationType, command.amount);
        });
        return future;
    }

    /**
     * 验证账户是否合法
     */
    @Subscribe
    public void handleAsync(CommandContext context, ValidateAccountCommand command) {
        ApplicationMessage applicationMessage = new AccountValidatePassedMessage(command.getAggregateRootId(), command.transactionId);
        //此处应该会调用外部接口验证账号是否合法，这里仅仅简单通过账号是否以INVALID字符串开头来判断是否合法；根据账号的合法性，返回不同的应用层消息
        if (command.getAggregateRootId().startsWith("INVALID")) {
            applicationMessage = new AccountValidateFailedMessage(command.getAggregateRootId(), command.transactionId, "账户不合法.");
        }
        context.setApplicationMessage(applicationMessage);
    }

    /**
     * 提交预操作
     */
    @Subscribe
    public CompletableFuture<BankAccount> handleAsync(CommandContext context, CommitTransactionPreparationCommand command) {
        CompletableFuture<BankAccount> future = context.getAsync(command.getAggregateRootId(), BankAccount.class);
        future.thenAccept(bankAccount -> {
            bankAccount.commitTransactionPreparation(command.transactionId);
        });
        return future;
    }
}

```

领域事件和`Saga`处理逻辑：

```java

/**
 * 银行存款交易流程管理器，用于协调银行存款交易流程中各个参与者聚合根之间的消息交互
 * IMessageHandler<DepositTransactionStartedEvent>,                    //存款交易已开始
 * IMessageHandler<DepositTransactionPreparationCompletedEvent>,       //存款交易已提交
 * IMessageHandler<TransactionPreparationAddedEvent>,                  //账户预操作已添加
 * IMessageHandler<TransactionPreparationCommittedEvent>               //账户预操作已提交
 */
@Event
public class DepositTransactionProcessManager {

    @Resource
    private CommandBus commandBus;

    @Subscribe
    public CompletableFuture<Boolean> handleAsync(DepositTransactionStartedEvent evnt) {
        AddTransactionPreparationCommand command = new AddTransactionPreparationCommand(evnt.accountId, evnt.getAggregateRootId(), TransactionType.DEPOSIT_TRANSACTION, PreparationType.CREDIT_PREPARATION, evnt.amount);
        command.setId(evnt.getId());
        return commandBus.sendAsync(command);
    }

    @Subscribe
    public CompletableFuture<SendMessageResult> handleAsync(TransactionPreparationAddedEvent evnt) {
        if (evnt.transactionPreparation.transactionType == TransactionType.DEPOSIT_TRANSACTION && evnt.transactionPreparation.preparationType == PreparationType.CREDIT_PREPARATION) {
            ConfirmDepositPreparationCommand command = new ConfirmDepositPreparationCommand(evnt.transactionPreparation.transactionId);
            command.setId(evnt.getId());
            return commandBus.sendAsync(command);
        }
        return Task.completedTask;
    }

    @Subscribe
    public CompletableFuture<SendMessageResult> handleAsync(DepositTransactionPreparationCompletedEvent evnt) {
        CommitTransactionPreparationCommand command = new CommitTransactionPreparationCommand(evnt.accountId, evnt.getAggregateRootId());
        command.setId(evnt.getId());
        return (commandBus.sendAsync(command));
    }

    @Subscribe
    public CompletableFuture<SendMessageResult> handleAsync(TransactionPreparationCommittedEvent evnt) {
        if (evnt.transactionPreparation.transactionType == TransactionType.DEPOSIT_TRANSACTION && evnt.transactionPreparation.preparationType == PreparationType.CREDIT_PREPARATION) {
            ConfirmDepositCommand command = new ConfirmDepositCommand(evnt.transactionPreparation.transactionId);
            command.setId(evnt.getId());
            return (commandBus.sendAsync(command));
        }
        return Task.completedTask;
    }
}

```