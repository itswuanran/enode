package org.enodeframework.commanding.impl

import org.enodeframework.commanding.ICommand
import org.enodeframework.commanding.ICommandContext
import org.enodeframework.commanding.ICommandHandlerProxy
import java.lang.invoke.MethodHandle
import java.lang.reflect.Method
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.reflect.jvm.kotlinFunction

/**
 * @author anruence@gmail.com
 */
class CommandHandlerProxy : ICommandHandlerProxy {
    private lateinit var innerObject: Any
    private lateinit var methodHandle: MethodHandle
    private lateinit var method: Method

    override suspend fun handleAsync(context: ICommandContext, command: ICommand) {
        if (method.kotlinFunction?.isSuspend == true) {
            invokeSuspend(getInnerObject(), context, command)
            return
        }
        methodHandle.invoke(getInnerObject(), context, command)
    }

    private suspend fun invokeSuspend(obj: Any, context: ICommandContext, command: ICommand): Any? = suspendCoroutineUninterceptedOrReturn { continuation ->
        methodHandle.invoke(obj, context, command, continuation)
    }

    override fun getInnerObject(): Any {
        return innerObject
    }

    override fun setInnerObject(innerObject: Any) {
        this.innerObject = innerObject
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