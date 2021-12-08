## 发版记录

### 1.1.1

- 常规版本升级，支持kotlin 1.6
- 修改测试最佳实践，默认配置有可能导致不必要的循环依赖
- 精简事件序列服务实现，冗余低效代码清理，提高可读性

### 1.1.0

- 重命名框架中接口格式，修改I开头的接口，更符合Java规范
- CommandService改名为CommandBus，更符合Command语义
- 支持CommandMessage传入不同的聚合根id基本类型

### 1.0.24

- 更新了`vert.x jdbc`驱动，支持传入自定义DataSource，为支持ShardingDataSource提供了便利
- 重构了`MySQL` `PostgreSQL` `MongoDB`驱动实现，重新抽象了`EventStore`和`PublishedVersionStore`的处理函数，简化代码
- **针对`Java`异步编程做了深度优化，支持`CommandHandler`和`EventHandler`中定义`CompletableFuture`返回值，阻塞调用封装在协程中，避免使用`#join() #get()`
  等阻塞代码，同时也支持kotlin suspend**
- 重新定义了线程模型，消费`mailbox`消息时使用递归的方式实现，无阻塞调用
- 针对web组件，可以完美支持`webflux`，返回值使用`Mono.fromFuture`包装既可
- 依赖基建版本升级，丰富了test case和压测case
