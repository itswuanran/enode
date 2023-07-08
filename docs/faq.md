
### 聚合根的定义

聚合根需要定义一个无参构造函数，因为聚合根初始化时使用了：

```java
aggregateRootType.getDeclaredConstructor().newInstance();
```

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
消息队列经过几次重构，把application和exception消息全部整合到domain event队列中去了，减少消息接入的复杂度
