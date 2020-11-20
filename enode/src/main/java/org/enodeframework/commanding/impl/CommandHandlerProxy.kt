package org.enodeframework.commanding.impl

import org.enodeframework.commanding.ICommand
import org.enodeframework.commanding.ICommandContext
import org.enodeframework.commanding.ICommandHandlerProxy
import org.enodeframework.common.container.ObjectContainer
import java.lang.invoke.MethodHandle
import java.lang.reflect.Method

/**
 * @author anruence@gmail.com
 */
class CommandHandlerProxy : ICommandHandlerProxy {
    private lateinit var handlerType: Class<*>
    private lateinit var methodHandle: MethodHandle
    private lateinit var method: Method
    override suspend fun handleAsync(context: ICommandContext, command: ICommand) {
        methodHandle.invoke(getInnerObject(), context, command)
    }

    override fun getInnerObject(): Any {
        return ObjectContainer.INSTANCE.resolve(handlerType)
    }

    override fun setHandlerType(handlerType: Class<*>) {
        this.handlerType = handlerType
    }

    override fun setMethodHandle(methodHandle: MethodHandle) {
        this.methodHandle = methodHandle
    }

    override fun getMethod(): Method {
        return method
    }

    override fun setMethod(method: Method) {
        this.method = method
    }
}