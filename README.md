## 框架简介

`enode`是基于`JVM`平台，为业务实践`Domain Driven Design`思想而落地的一个应用框架，使用`CQRS`, `Event Sourcing`设计模式，让开发者可以专注于业务模型建模和业务逻辑开发

## 框架特色
- `Reactive`
  - `enode`是完全响应式的框架，是实现高吞吐的核心设计
  - `db`层面使用了异步驱动，实现全链路异步
  - 针对`IO`密集型操作，集成了`kotlin coroutine`
- `Event Sourcing`
  - 聚合根的事件完全持久化，记录聚合根的状态变化，让`C`端的数据可追溯，数据持久化变得通用化
- `Event Driven`
  - 业务流程以事件驱动为核心，让研发更聚焦业务中领域事件的建设和积累
  - 先进的`Saga`机制，以事件驱动的流程管理器（`Process Manager`）的方式支持一个用户操作跨多个聚合根的业务场景，如订单处理，从而避免分布式事务的使用
- `CQRS`
  - 基于`CQRS`架构思想，`enode`解决了`CQRS`架构的`C`端的高并发写的问题，以及`CQ`两端数据同步的顺序性保证和幂等性 
  - 支持`Fire And Forget`和`Fire And Wait`两种方式返回命令执行结果
- `Fast and Flexible`
  - 聚合根常驻内存（`In-Memory Domain Model`），在设计上尽可能的避免了聚合根重建，可以完全以`OO`的方式来设计实现聚合根，不必为`ORM`的阻抗失衡而烦恼
  - 聚合根的处理基于`Actor`思想，乐观并发控制，无锁，在事件持久化层面使用`Group Commit Domain Event`提高写性能

`enode`在架构层面严格规范了研发人员该如何写代码，要求用`DDD`的方式思考，严格遵守聚合内强一致性、聚合之间最终一致性的原则

在设计上遵循`SOLID`，以下均可扩展替换成自建
- 针对`IoC`容器，目前`SpringBoot`友好适配
- 针对`CommandBus`，只要求最基础的队列能力，目前适配了`Kafka`、`RocketMQ`、`Pulsar`、`AMQP`
- 针对`EventStore`，适配了`MySQL`、`PostgreSQL`、`MongoDB`
- 针对`ReplyService`，要求实现点对点通信模型，实现Command处理结果的通知

## 使用约束

- **一个**命令一次只能修改**一个**聚合根
- **聚合间**只能通过**领域消息**交互
- 聚合内**强一致性**
- 聚合间**最终一致性**

## 发版记录

[CHANGELOG](CHANGELOG.md)

## 整体架构

![](enode-arch.jpg)

## `Saga`的两种模式

- 编排（`Choreography`）
  参与者（子事务）之间的调用、分配、决策和排序，通过交换事件进行进行。是一种去中心化的模式，参与者之间通过消息机制进行沟通，通过监听器的方式监听其他参与者发出的消息，从而执行后续的逻辑处理。

> `enode`中使用的就是这种模式

- 控制（`Orchestration`）
  提供一个控制类，方便参与者之间的协调工作。事务执行的命令从控制类发起，按照逻辑顺序请求`Saga`
  的参与者，从参与者那里接受到反馈以后，控制类在发起向其他参与者的调用。所有`Saga`的参与者都围绕这个控制类进行沟通和协调工作。

> [`Apache ServiceComb`](https://servicecomb.apache.org/) 使用的是这种模式

### 编程模型

新增了三个注解，系统限定了只扫描`@Command`和`@Event`标识的类，执行的方法上需要添加`@Subscribe`注解：

- `@Command`
- `@Event`
- `@Subscribe`

启动时会扫描包路径下的注解，注册成`Spring Bean`，和`@Component`作用相同。
## 基础组件依赖

### `docker`部署
定义了`docker-compose.yml`，搭配`docker-compose`快速的在启动一套运行环境，用于应用的测试

```bash
docker-compose up -d
```

## 消息

- 目前enode函数调用的实现是放在`kotlin coroutine`中来执行的，这里涉及到实际执行的任务类型，针对计算密集型和IO密集型的任务，目前没有做可定制化的配置，后续的版本会考虑加上，
  **使用也很简单，`@Subscribe` 方法体加上`suspend`标记即可**。

- **针对`Java`异步编程做了深度优化，支持`CommandHandler`和`EventHandler`中定义`CompletableFuture`
  返回值，阻塞调用封装在协程中，避免使用`#join() #get()`等阻塞代码，同时也支持`kotlin suspend`**

## 详细介绍

- [enode核心概念介绍](./docs/intro.md)
- [enode使用说明](./docs/usage.md)

## 最佳实践

可参考[samples](samples)模块中的例子 

转账的例子中，转账的业务场景，涉及了三个聚合根：
- 银行存款交易记录，表示一笔银行存款交易
- 银行转账交易记录，表示一笔银行内账户之间的转账交易
- 银行账户聚合根，封装银行账户余额变动的数据一致性

### `command-web`启动

- `CQRS`架构中的`Command`端应用

主要用来接收来自用户的`Command`请求，核心是将`Command`发送到消息队列。

### `command-consumer`启动

- 消费`Command`队列中的消息的服务

> 将领域事件消息持久化才算是`Command`执行成功，`Command`执行的结果可以通过发送命令时注册的监听器获取。

### `event-consumer`启动

- 领域事件处理服务

> 事件可能会多次投递，所以需要消费端逻辑保证幂等处理，这里框架无法完成支持，需要开发者自己实现。

## See Also
[conference](https://github.com/anruence/conference)

## 参考项目

- https://github.com/tangxuehua/enode
- https://github.com/coffeewar/enode-master
