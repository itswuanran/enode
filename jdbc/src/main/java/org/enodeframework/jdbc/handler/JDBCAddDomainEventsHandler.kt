package org.enodeframework.jdbc.handler

import com.google.common.base.Strings
import com.google.common.collect.Lists
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import org.enodeframework.common.exception.EventStoreException
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.configurations.EventStoreOptions
import org.enodeframework.eventing.AggregateEventAppendResult
import org.enodeframework.eventing.EventAppendStatus
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.util.concurrent.CompletableFuture

open class JDBCAddDomainEventsHandler(
    private val options: EventStoreOptions, private val msg: String
) : Handler<AsyncResult<RowSet<Row>>> {

    companion object {
        private val logger = LoggerFactory.getLogger(JDBCAddDomainEventsHandler::class.java)
    }

    var future = CompletableFuture<AggregateEventAppendResult>()

    override fun handle(ar: AsyncResult<RowSet<Row>>) {
        if (ar.succeeded()) {
            val appendResult = AggregateEventAppendResult(EventAppendStatus.Success)
            future.complete(appendResult)
            return
        }
        val ex = ar.cause()
        var throwable = ex
        if (ex is SQLException) {
            throwable = ex;
        }
        if (ex.cause is SQLException) {
            throwable = ex.cause
        }
        if (throwable.message?.contains(options.eventVersionUkName) == true) {
            val appendResult = AggregateEventAppendResult(EventAppendStatus.DuplicateEvent)
            future.complete(appendResult)
            return
        }
        if (throwable.message?.contains(options.eventCommandIdUkName) == true) {
            // 不同的数据库在冲突时的错误信息不同，可以通过解析错误信息的方式将冲突的commandId找出来，这里要求id不能命中正则的规则（不包含-字符）
            val appendResult = AggregateEventAppendResult(EventAppendStatus.DuplicateCommand)
            val message = throwable.message ?: ""
            val commandId = options.parseDuplicatedId(message)
            if (!Strings.isNullOrEmpty(commandId)) {
                appendResult.duplicateCommandIds = Lists.newArrayList(commandId)
            } else {
                appendResult.duplicateCommandIds = Lists.newArrayList(message)
            }
            future.complete(appendResult)
            return
        }
        logger.error("Batch append event has exception. {}", msg, throwable)
        if (throwable is SQLException) {
            future.completeExceptionally(IORuntimeException(throwable))
            return
        }
        future.completeExceptionally(EventStoreException(throwable))
        return
    }
}
