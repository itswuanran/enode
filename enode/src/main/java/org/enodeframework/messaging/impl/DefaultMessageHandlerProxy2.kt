package org.enodeframework.messaging.impl

import kotlinx.coroutines.future.asDeferred
import org.enodeframework.messaging.Message
import org.enodeframework.messaging.MessageHandlerProxy2
import java.lang.invoke.MethodHandle
import java.lang.reflect.Method
import java.util.concurrent.CompletionStage
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.reflect.jvm.kotlinFunction

/**
 * @author anruence@gmail.com
 */
class DefaultMessageHandlerProxy2 : MessageHandlerProxy2 {
    private lateinit var innerObject: Any
    private lateinit var methodHandle: MethodHandle
    private lateinit var method: Method

    override suspend fun handleAsync(message1: Message, message2: Message) {
        val methodParameterTypes = method.parameterTypes
        if (method.kotlinFunction?.isSuspend == true) {
            if (methodParameterTypes[0].isAssignableFrom(message1.javaClass)) {
                invokeSuspend(getInnerObject(), message1, message2)
            } else {
                invokeSuspend(getInnerObject(), message2, message1)
            }
            return
        }
        if (methodParameterTypes[0].isAssignableFrom(message1.javaClass)) {
            val result = methodHandle.invoke(getInnerObject(), message1, message2)
            if (result is CompletionStage<*>) {
                result.asDeferred().await()
            }
        } else {
            val result = methodHandle.invoke(getInnerObject(), message2, message1)
            if (result is CompletionStage<*>) {
                result.asDeferred().await()
            }
        }
    }

    private suspend fun invokeSuspend(obj: Any, message1: Message, message2: Message): Any? =
        suspendCoroutineUninterceptedOrReturn { continuation ->
            methodHandle.invoke(obj, message1, message2, continuation)
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