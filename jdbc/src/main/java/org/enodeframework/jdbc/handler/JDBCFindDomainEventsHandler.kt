package org.enodeframework.jdbc.handler

import com.google.common.collect.Maps
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import org.enodeframework.common.exception.EventStoreException
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.serializing.SerializeService
import org.enodeframework.common.utils.DateUtil
import org.enodeframework.eventing.DomainEventStream
import org.enodeframework.eventing.EventSerializer
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.util.concurrent.CompletableFuture

class JDBCFindDomainEventsHandler(
    private val eventSerializer: EventSerializer,
    private val serializeService: SerializeService,
    private val msg: String
) : Handler<AsyncResult<RowSet<Row>>> {

    companion object {
        private val logger = LoggerFactory.getLogger(JDBCFindDomainEventsHandler::class.java)
    }

    var future = CompletableFuture<List<DomainEventStream>>()

    override fun handle(ar: AsyncResult<RowSet<Row>>) {
        if (ar.succeeded()) {
            future.complete(ar.result().map { row: Row ->
                this.convertFrom(row.toJson())
            }.toList())
            return
        }
        val throwable = ar.cause()
        if (throwable is SQLException) {
            logger.error(
                "Find event has sql exception, msg: {}", msg, throwable
            )
            future.completeExceptionally(IORuntimeException(msg, throwable))
            return
        }
        logger.error(
            "Find event by has unknown exception, msg: {}", msg, throwable
        )
        future.completeExceptionally(EventStoreException(msg, throwable))
        return
    }

    private fun convertFrom(record: JsonObject): DomainEventStream {
        val gmtCreate = record.getValue("gmt_create")
        val date = DateUtil.parseDate(gmtCreate)
        return DomainEventStream(
            record.getString("command_id"),
            record.getString("aggregate_root_id"),
            record.getString("aggregate_root_type_name"),
            date,
            eventSerializer.deserialize(
                serializeService.deserialize(
                    record.getString("events"), MutableMap::class.java
                ) as MutableMap<String, String>
            ),
            Maps.newHashMap()
        )
    }
}