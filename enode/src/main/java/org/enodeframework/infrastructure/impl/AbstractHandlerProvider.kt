package org.enodeframework.infrastructure.impl

import org.enodeframework.annotation.Command
import org.enodeframework.annotation.Event
import org.enodeframework.annotation.Priority
import org.enodeframework.annotation.Subscribe
import org.enodeframework.common.container.ObjectContainer
import org.enodeframework.common.exception.HandlerRegisterException
import org.enodeframework.infrastructure.AssemblyInitializer
import org.enodeframework.infrastructure.MethodInvocation
import org.enodeframework.infrastructure.ObjectProxy
import org.enodeframework.messaging.MessageHandlerData
import org.reflections.ReflectionUtils
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.reflect.jvm.kotlinFunction

abstract class AbstractHandlerProvider<TKey, THandlerProxyInterface, THandlerSource> :
    AssemblyInitializer where THandlerProxyInterface : ObjectProxy, THandlerProxyInterface : MethodInvocation {
    private val handlerDict: MutableMap<TKey, MutableList<THandlerProxyInterface>> = HashMap()
    private val messageHandlerDict: MutableMap<TKey, MessageHandlerData<THandlerProxyInterface>> = HashMap()
    private val lookup = MethodHandles.lookup()

    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * handlerDict中注册的key
     */
    protected abstract fun getKey(method: Method): TKey

    /**
     * 代理执行的类型
     */
    protected abstract fun getHandlerProxyImplementationType(): Class<out THandlerProxyInterface>

    /**
     * 判定执行类型是否匹配
     */
    protected abstract fun isHandlerSourceMatchKey(handlerSource: THandlerSource, key: TKey): Boolean

    /**
     * kotlin suspend 方法会多一个参数 kotlin.coroutines.Continuation
     */
    protected abstract fun isHandleMethodMatch(method: Method): Boolean

    /**
     * 在启动时校验一个消息是否可以注册多个处理器
     */
    protected abstract fun isHandleRegisterOnce(): Boolean

    /**
     * 是否是一个可挂起的方法
     */
    protected open fun isSuspendMethod(method: Method): Boolean {
        return method.kotlinFunction?.isSuspend == true
    }

    override fun initialize(objectContainer: ObjectContainer, componentTypes: Set<Class<*>>) {
        componentTypes.filter { type: Class<*> -> isHandlerType(type) }
            .forEach { handlerType: Class<*> -> registerHandler(objectContainer, handlerType) }
        initializeHandlerPriority()
    }

    fun getHandlersInternal(source: THandlerSource): List<MessageHandlerData<THandlerProxyInterface>> {
        return messageHandlerDict.keys.filter { key: TKey -> isHandlerSourceMatchKey(source, key) }
            .map { key: TKey -> messageHandlerDict.getValue(key) }
    }

    private fun initializeHandlerPriority() {
        handlerDict.forEach { (key: TKey, handlers: List<THandlerProxyInterface>) ->
            val handlerData = MessageHandlerData<THandlerProxyInterface>()
            val listHandlers: MutableList<THandlerProxyInterface> = ArrayList()
            val queueHandlerDict: MutableMap<THandlerProxyInterface, Int> = HashMap()
            handlers.forEach { handler: THandlerProxyInterface ->
                val priority = getHandleMethodPriority(handler)
                if (priority == 0) {
                    listHandlers.add(handler)
                } else {
                    queueHandlerDict[handler] = priority
                }
            }
            handlerData.allHandlers = handlers
            handlerData.listHandlers = listHandlers
            handlerData.queuedHandlers =
                queueHandlerDict.entries.sortedWith(Comparator.comparingInt { v -> v.value }).map { x -> x.key }
            messageHandlerDict[key] = handlerData
        }
    }

    private fun getHandleMethodPriority(handler: THandlerProxyInterface): Int {
        val method = handler.getMethod()
        var priority = 0
        val methodPriority = method.getAnnotation(Priority::class.java)
        if (methodPriority != null) {
            priority = methodPriority.value
        }
        if (priority == 0) {
            val classPriority = handler.getInnerObject().javaClass.getAnnotation(Priority::class.java)
            if (classPriority != null) {
                priority = classPriority.value
            }
        }
        return priority
    }

    private fun isHandlerType(type: Class<*>): Boolean {
        if (type.isInterface) {
            return false
        }
        return if (Modifier.isAbstract(type.modifiers)) {
            false
        } else containsHandleType(type)
    }

    private fun containsHandleType(type: Class<*>): Boolean {
        return type.isAnnotationPresent(Command::class.java) || type.isAnnotationPresent(Event::class.java)
    }

    protected fun isMethodAnnotationSubscribe(method: Method): Boolean {
        return method.isAnnotationPresent(Subscribe::class.java)
    }

    private fun registerHandler(objectContainer: ObjectContainer, handlerType: Class<*>) {
        val handleMethods = ReflectionUtils.getMethods(handlerType, { method: Method -> isHandleMethodMatch(method) })
        handleMethods.forEach { method: Method ->
            // 反射Method转换为MethodHandle，提高效率
            val handleMethod = lookup.findVirtual(
                handlerType, method.name, MethodType.methodType(method.returnType, method.parameterTypes)
            )
            val key = this.getKey(method)
            val handlers = handlerDict.computeIfAbsent(key) { ArrayList() }
            val handlerProxy = this.getHandlerProxyImplementationType().getDeclaredConstructor().newInstance()
            handlerProxy.setInnerObject(objectContainer.resolve(handlerType))
            handlerProxy.setMethod(method)
            handlerProxy.setMethodHandle(handleMethod)
            handlers.add(handlerProxy)
            // 针对command，只允许一个处理器
            if (isHandleRegisterOnce() && handlers.size > 1) {
                throw HandlerRegisterException("${method.name}#${method.parameterTypes.joinToString(",")}")
            }
            logger.info(
                "Handler register success. {} => {}",
                key,
                "${handlerType.name}#${method.name}#${method.parameterTypes.joinToString(",")}"
            )
        }
    }
}