package com.enodeframework.infrastructure;

import java.util.List;

public interface ITwoMessageHandlerProvider {
    List<MessageHandlerData<IMessageHandlerProxy2>> getHandlers(List<Class> messageTypes);
}
