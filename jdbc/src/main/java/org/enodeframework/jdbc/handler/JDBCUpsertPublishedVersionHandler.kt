package org.enodeframework.jdbc.handler

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.exception.PublishedVersionStoreException
import org.enodeframework.eventing.EventStoreConfiguration
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.util.concurrent.CompletableFuture

open class JDBCUpsertPublishedVersionHandler(
    private val options: EventStoreConfiguration, private val msg: String
) : Handler<AsyncResult<RowSet<Row>>> {

    private val logger = LoggerFactory.getLogger(JDBCUpsertPublishedVersionHandler::class.java)

    val future = CompletableFuture<Int>()

    override fun handle(ar: AsyncResult<RowSet<Row>>) {

        if (ar.succeeded()) {
            if (ar.result().rowCount() == 0) {
                future.completeExceptionally(
                    PublishedVersionStoreException(
                        String.format(
                            "version update rows is 0. %s", msg
                        )
                    )
                )
                return
            }
            future.complete(ar.result().rowCount())
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
        logger.error("Upsert aggregate published version has exception. {}", msg, throwable)
        if (throwable.message?.contains(options.publishedUkName) == true) {
            future.complete(1)
            return
        }
        if (throwable is SQLException) {
            future.completeExceptionally(IORuntimeException(msg, throwable))
        }
        return
    }
}