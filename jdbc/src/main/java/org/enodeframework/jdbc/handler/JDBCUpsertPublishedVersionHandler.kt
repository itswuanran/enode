package org.enodeframework.jdbc.handler

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.exception.PublishedVersionStoreException
import org.enodeframework.eventing.EventStoreOptions
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.util.concurrent.CompletableFuture

open class JDBCUpsertPublishedVersionHandler(
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