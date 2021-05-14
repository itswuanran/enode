package org.enodeframework.common.io

import com.alibaba.csp.sentinel.SphU
import com.alibaba.csp.sentinel.Tracer
import com.alibaba.csp.sentinel.slots.block.BlockException
import io.reactivex.rxjava3.core.Flowable
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

abstract class AbstractAsyncCommand<R> {
    /**
     * command暴露给外部的异步执行函数，
     *
     * @return 返回CompletableFuture
     */
    fun queue(): CompletableFuture<R> {
        val future = CompletableFuture<R>()
        try {
            val entry = SphU.asyncEntry(resourceKey)
            construct().subscribe({ x: R ->
                try {
                    future.complete(x)
                } finally {
                    entry.exit()
                }
            }) { exception: Throwable? ->
                try {
                    fallback(future)
                    Tracer.trace(exception)
                    LOGGER.error("failed to execute command {}", resourceKey, exception)
                } finally {
                    entry.exit()
                }
            }
        } catch (blockException: BlockException) {
            fallback(future)
        }
        return future
    }
    /**
     * command暴露给外部的同步执行函数
     *
     * @param timeout 执行等待的超时时间
     * @return 返回执行结果
     */
    /**
     * command暴露给外部的同步执行函数
     *
     * @return 返回类型为结果类型，阻塞执行，默认超时5s
     */
    @JvmOverloads
    fun execute(timeout: Long = DEFAULT_EXECUTION_TIMEOUT.toLong()): R {
        return try {
            queue()[timeout, TimeUnit.MILLISECONDS]
        } catch (e: Exception) {
            LOGGER.error("Failed to execute {}", resourceKey, e)
            fallbackResult
        }
    }

    // 需要子类提供差异化实现的方法
    protected abstract val resourceKey: String
    protected abstract fun construct(): Flowable<R>
    protected abstract val fallbackResult: R
    private fun fallback(future: CompletableFuture<R>) {
        try {
            future.complete(fallbackResult)
        } catch (e: Exception) {
            future.completeExceptionally(e)
        }
    }

    companion object {
        // 外部调用兜底超时时间
        protected const val DEFAULT_EXECUTION_TIMEOUT = 5000
        private val LOGGER = LoggerFactory.getLogger(AbstractAsyncCommand::class.java)
    }
}