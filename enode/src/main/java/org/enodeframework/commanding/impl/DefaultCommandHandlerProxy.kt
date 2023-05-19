package org.enodeframework.commanding.impl

import kotlinx.coroutines.future.await
import org.enodeframework.commanding.CommandContext
import org.enodeframework.commanding.CommandHandlerProxy
import org.enodeframework.commanding.CommandMessage
import java.lang.invoke.MethodHandle
import java.lang.reflect.Method
import java.util.concurrent.CompletionStage
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.reflect.jvm.kotlinFunction

/**
 * @author anruence@gmail.com
 */
class DefaultCommandHandlerProxy : CommandHandlerProxy {
    private lateinit var innerObject: Any
    private lateinit var methodHandle: MethodHandle
    private lateinit var method: Method

    override suspend fun handleAsync(context: CommandContext, command: CommandMessage) {
        if (method.kotlinFunction?.isSuspend == true) {
            invokeSuspend(getInnerObject(), context, command)
            return
        }
        val result = methodHandle.invoke(getInnerObject(), context, command)
        if (result is CompletionStage<*>) {
            result.await()
        }
    }

    private suspend fun invokeSuspend(obj: Any, context: CommandContext, command: CommandMessage): Any? =
        suspendCoroutineUninterceptedOrReturn { continuation ->
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