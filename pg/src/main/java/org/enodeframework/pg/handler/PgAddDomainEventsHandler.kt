package org.enodeframework.pg.handler

import com.google.common.base.Strings
import com.google.common.collect.Lists
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.pgclient.PgException
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import org.enodeframework.common.exception.EventStoreException
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.eventing.AggregateEventAppendResult
import org.enodeframework.eventing.EventAppendStatus
import org.enodeframework.eventing.EventStoreOptions
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

class PgAddDomainEventsHandler(
    private val options: EventStoreOptions,
    private val msg: String,
) : Handler<AsyncResult<RowSet<Row>>> {

    private val logger = LoggerFactory.getLogger(PgAddDomainEventsHandler::class.java)

    val future = CompletableFuture<AggregateEventAppendResult>()

    override fun handle(ar: AsyncResult<RowSet<Row>>) {
        if (ar.succeeded()) {
            val appendResult = AggregateEventAppendResult(EventAppendStatus.Success)
            future.complete(appendResult)
            return
        }
        val throwable = ar.cause()
        var message = ""
        var detail = ""
        if (throwable is PgException) {
            message = throwable.message ?: ""
            detail = throwable.detail
        }
        if (throwable.cause is PgException) {
            message = (throwable.cause as PgException).message ?: ""
            detail = (throwable.cause as PgException).detail
        }
        if (message.contains(options.eventVersionUkName)) {
            val appendResult = AggregateEventAppendResult(EventAppendStatus.DuplicateEvent)
            future.complete(appendResult)
            return
        }
        if (message.contains(options.eventCommandIdUkName)) {
            // 不同的数据库在冲突时的错误信息不同，可以通过解析错误信息的方式将冲突的commandId找出来，这里要求id不能命中正则的规则（不包含-字符）
            val appendResult = AggregateEventAppendResult(EventAppendStatus.DuplicateCommand)
            if (throwable is PgException) {
                val commandId = options.seekCommandId(detail)
                if (!Strings.isNullOrEmpty(commandId)) {
                    appendResult.duplicateCommandIds = Lists.newArrayList(commandId)
                } else {
                    appendResult.duplicateCommandIds = Lists.newArrayList(message)
                }
            }
            future.complete(appendResult)
            return
        }
        logger.error("Batch append event has exception.{}", msg, throwable)
        if (throwable is PgException) {
            future.completeExceptionally(IORuntimeException(throwable))
            return
        }
        future.completeExceptionally(EventStoreException(throwable))
        return
    }
}
