package org.enodeframework.mysql.handler

import com.google.common.base.Strings
import com.google.common.collect.Lists
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.mysqlclient.MySQLException
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import org.enodeframework.common.exception.EventStoreException
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.configurations.EventStoreOptions
import org.enodeframework.eventing.AggregateEventAppendResult
import org.enodeframework.eventing.EventAppendStatus
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

open class MySQLAddDomainEventsHandler(
    private val options: EventStoreOptions,
    private val msg: String,
) :
    Handler<AsyncResult<RowSet<Row>>> {

    companion object {
        private val logger = LoggerFactory.getLogger(MySQLAddDomainEventsHandler::class.java)
    }

    val future = CompletableFuture<AggregateEventAppendResult>()

    private fun getDuplicatedId(message: String): String {
        val matcher = options.commandIdPattern.matcher(message)
        if (!matcher.find()) {
            return ""
        }
        return if (matcher.groupCount() == 0) {
            ""
        } else matcher.group(1)
    }

    override fun handle(ar: AsyncResult<RowSet<Row>>) {
        if (ar.succeeded()) {
            val appendResult = AggregateEventAppendResult()
            appendResult.eventAppendStatus = EventAppendStatus.Success
            future.complete(appendResult)
            return
        }
        val ex = ar.cause()
        var throwable = ex
        if (ex is MySQLException) {
            throwable = ex;
        }
        if (ex.cause is MySQLException) {
            throwable = ex.cause
        }
        if (throwable.message?.contains(options.eventVersionUkName) == true) {
            val appendResult = AggregateEventAppendResult()
            appendResult.eventAppendStatus = EventAppendStatus.DuplicateEvent
            future.complete(appendResult)
            return
        }
        if (throwable.message?.contains(options.eventCommandIdUkName) == true) {
            // 不同的数据库在冲突时的错误信息不同，可以通过解析错误信息的方式将冲突的commandId找出来，这里要求id不能命中正则的规则（不包含-字符）
            val appendResult = AggregateEventAppendResult()
            appendResult.eventAppendStatus = EventAppendStatus.DuplicateCommand
            val commandId = this.getDuplicatedId(throwable.message ?: "")
            if (!Strings.isNullOrEmpty(commandId)) {
                appendResult.duplicateCommandIds = Lists.newArrayList(commandId)
            }
            // 如果没有从异常信息获取到commandId(很低概率获取不到)，需要从db查询出来
            // 但是Vert.x的线程模型决定了不能再次使用EventLoop线程执行阻塞的查询操作
            future.complete(appendResult)
            return
        }
        logger.error("Batch append event has exception. {}", msg, throwable)
        if (throwable is MySQLException) {
            future.completeExceptionally(IORuntimeException(throwable))
            return
        }
        future.completeExceptionally(EventStoreException(throwable))
        return
    }
}
