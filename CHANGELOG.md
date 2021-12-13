## 发版记录

### 1.1.2

- 删除`application`和`exception`消息队列，统一发送到`event`队列
- 获取类名时修改为使用TypeNameProvider，取消实现的强依赖
- 聚合根的`EventHandler`重构，每次重新从`Container`中获取
- `feature`: 支持自定义`CoroutineDispatcher`，留出扩展点，可进行`Wrap`支持`trace`等信息传递

### 1.1.1

- 常规版本升级，支持`kotlin 1.6`
- 修改测试最佳实践，默认配置有可能导致不必要的循环依赖
- 精简事件序列服务实现，冗余低效代码清理，提高可读性
- 整合测试用例，覆盖多选型场景
- `bugfix`: 发布事件时需要重新合并`Command`的`items`
- `bugfix`: 修复`InMemoryEventStore`写入失效的问题

### 1.1.0

- 重命名框架中接口格式，修改I开头的接口，更符合`Java`规范
- `CommandService`改名为`CommandBus`，更符合`Command`语义
- 支持`CommandMessage`传入不同的聚合根`id`基本类型

### 1.0.24

- 更新了`vert.x jdbc`驱动，支持传入自定义`DataSource`，为支持`ShardingDataSource`提供了便利
- 重构了`MySQL` `PostgreSQL` `MongoDB`驱动实现，重新抽象了`EventStore`和`PublishedVersionStore`的处理函数，简化代码
- **针对`Java`异步编程做了深度优化，支持`CommandHandler`和`EventHandler`中定义`CompletableFuture`返回值，阻塞调用封装在协程中，避免使用`#join() #get()`
  等阻塞代码，同时也支持`kotlin suspend`**
- 重新定义了线程模型，消费`mailbox`消息时使用递归的方式实现，无阻塞调用
- 针对web组件，可以完美支持`webflux`，返回值使用`Mono.fromFuture`包装既可
- 依赖基建版本升级，丰富了`test case`和压测`case`
