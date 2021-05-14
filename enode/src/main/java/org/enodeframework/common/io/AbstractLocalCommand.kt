package org.enodeframework.common.io

/**
 * 默认实现结果本地化方法
 *
 * @param <R>
 */
abstract class AbstractLocalCommand<R> : AbstractAdvanceLocalCommand<R, R>() {
    override fun convertResult(rpcResult: R): R {
        return rpcResult
    }
}