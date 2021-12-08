package org.enodeframework.jdbc.handler

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.exception.PublishedVersionStoreException
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.util.concurrent.CompletableFuture

class JDBCFindPublishedVersionHandler(private val msg: String) : Handler<AsyncResult<RowSet<Row>>> {

    companion object {
        private val logger = LoggerFactory.getLogger(JDBCFindPublishedVersionHandler::class.java)
    }

    var future = CompletableFuture<Int>()

    override fun handle(ar: AsyncResult<RowSet<Row>>) {
        if (ar.succeeded()) {
            future.complete(ar.result().firstOrNull()?.getInteger(0) ?: 0)
            return
        }
        val throwable = ar.cause()
        if (throwable is SQLException) {
            logger.error(
                "Get aggregate published version has sql exception. msg: {}", msg, throwable
            )
            future.completeExceptionally(IORuntimeException(msg, throwable))
            return
        }
        logger.error(
            "Get aggregate published version has unknown exception. msg: {}", msg, throwable
        )
        future.completeExceptionally(PublishedVersionStoreException(msg, throwable))
        return
    }
}