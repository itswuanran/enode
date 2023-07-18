package org.enodeframework.jdbc

import com.google.common.base.Strings
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import org.enodeframework.common.exception.EventStoreException
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.exception.PublishedVersionStoreException
import org.enodeframework.common.serializing.SerializeService
import org.enodeframework.eventing.AggregateEventAppendResult
import org.enodeframework.eventing.DomainEventStream
import org.enodeframework.eventing.EventAppendStatus
import org.enodeframework.eventing.EventSerializer
import org.enodeframework.eventing.EventStoreOptions
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.util.*
import java.util.concurrent.CompletableFuture

class JDBCAddDomainEventsHandler(
    private val options: EventStoreOptions, private val msg: String
) : Handler<AsyncResult<RowSet<Row>>> {

    private val logger = LoggerFactory.getLogger(JDBCAddDomainEventsHandler::class.java)

    var future = CompletableFuture<AggregateEventAppendResult>()

    override fun handle(ar: AsyncResult<RowSet<Row>>) {
        if (ar.succeeded()) {
            val appendResult = AggregateEventAppendResult(EventAppendStatus.Success)
            future.complete(appendResult)
            return
        }
        val throwable = ar.cause()
        var message = ""
        if (throwable is SQLException) {
            message = throwable.message ?: ""
        }
        if (throwable.cause is SQLException) {
            message = (throwable.cause as SQLException).message ?: ""
        }
        if (message.contains(options.eventVersionUkName)) {
            val appendResult = AggregateEventAppendResult(EventAppendStatus.DuplicateEvent)
            future.complete(appendResult)
            return
        }
        if (message.contains(options.eventCommandIdUkName)) {
            // 不同的数据库在冲突时的错误信息不同，可以通过解析错误信息的方式将冲突的commandId找出来，这里要求id不能命中正则的规则（不包含-字符）
            val appendResult = AggregateEventAppendResult(EventAppendStatus.DuplicateCommand)
            val commandId = options.seekCommandId(message)
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

class JDBCFindDomainEventsHandler(
    private val eventSerializer: EventSerializer,
    private val serializeService: SerializeService,
    private val msg: String
) : Handler<AsyncResult<RowSet<Row>>> {

    private val logger = LoggerFactory.getLogger(JDBCFindDomainEventsHandler::class.java)

    var future = CompletableFuture<List<DomainEventStream>>()

    override fun handle(ar: AsyncResult<RowSet<Row>>) {
        if (ar.succeeded()) {
            future.complete(ar.result().map { row: Row ->
                this.convertFrom(row.toJson())
            }.toList())
            return
        }
        val throwable = ar.cause()
        logger.error("Find event has exception, msg: {}", msg, throwable)
        if (throwable is SQLException) {
            future.completeExceptionally(IORuntimeException(msg, throwable))
            return
        }
        future.completeExceptionally(EventStoreException(msg, throwable))
        return
    }

    private fun convertFrom(record: JsonObject): DomainEventStream {
        return DomainEventStream(
            record.getString("command_id"),
            record.getString("aggregate_root_id"),
            record.getString("aggregate_root_type_name"),
            Date(record.getLong("create_at")),
            eventSerializer.deserialize(
                serializeService.deserialize(
                    record.getString("events"), MutableMap::class.java
                ) as MutableMap<String, String>
            ),
            Maps.newHashMap()
        )
    }
}

class JDBCFindPublishedVersionHandler(private val msg: String) : Handler<AsyncResult<RowSet<Row>>> {

    private val logger = LoggerFactory.getLogger(JDBCFindPublishedVersionHandler::class.java)

    var future = CompletableFuture<Int>()

    override fun handle(ar: AsyncResult<RowSet<Row>>) {
        if (ar.succeeded()) {
            future.complete(ar.result().firstOrNull()?.getInteger(0) ?: 0)
            return
        }
        val throwable = ar.cause()
        logger.error("Find aggregate published version has exception. msg: {}", msg, throwable)
        if (throwable is SQLException) {
            future.completeExceptionally(IORuntimeException(msg, throwable))
            return
        }
        future.completeExceptionally(PublishedVersionStoreException(msg, throwable))
        return
    }
}

class JDBCUpsertPublishedVersionHandler(
    private val options: EventStoreOptions, private val msg: String
) : Handler<AsyncResult<RowSet<Row>>> {

    private val logger = LoggerFactory.getLogger(JDBCUpsertPublishedVersionHandler::class.java)

    val future = CompletableFuture<Int>()
    override fun handle(ar: AsyncResult<RowSet<Row>>) {
        if (ar.succeeded()) {
            if (ar.result().rowCount() == 0) {
                future.completeExceptionally(
                    PublishedVersionStoreException(
                        "version update rows is 0. $msg"
                    )
                )
                return
            }
            future.complete(ar.result().rowCount())
            return
        }
        val throwable = ar.cause()
        var message = ""
        if (throwable is SQLException) {
            message = throwable.message ?: ""
        }
        if (throwable.cause is SQLException) {
            message = (throwable.cause as SQLException).message ?: ""
        }
        if (message.contains(options.publishedUkName)) {
            future.complete(1)
            return
        }
        logger.error("Upsert aggregate published version has exception. {}", msg, throwable)
        if (throwable is SQLException) {
            future.completeExceptionally(IORuntimeException(msg, throwable))
            return
        }
        future.completeExceptionally(PublishedVersionStoreException(msg, throwable))
        return
    }
}