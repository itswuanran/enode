package org.enodeframework.common.io

import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.subjects.ReplaySubject
import io.reactivex.rxjava3.subjects.Subject

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

/**
 * 同步调用通过本地线程池封装成异步调用
 *
 * @param <R> 返回的本地对象
 * @param <T> RPC返回的结果
 */
abstract class AbstractAdvanceLocalCommand<T, R> : AbstractAsyncCommand<R>() {

    override fun construct(): Flowable<R> {
        val subject: Subject<R> = ReplaySubject.create()
        try {
            CoroutineScope(Dispatchers.IO).async {
                try {
                    val rpcResult = callMethod()
                    val result = convertResult(rpcResult);
                    subject.onNext(result)
                    subject.onComplete()
                } catch (e: Exception) {
                    subject.onError(e)
                }
            }
        } catch (exception: Exception) {
            subject.onError(exception)
        }
        return subject.toFlowable(BackpressureStrategy.BUFFER)
    }

    protected abstract fun callMethod(): T

    protected abstract fun convertResult(rpcResult: T): R
}