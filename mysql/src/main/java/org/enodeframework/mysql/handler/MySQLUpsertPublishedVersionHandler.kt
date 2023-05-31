package org.enodeframework.mysql.handler

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.mysqlclient.MySQLBatchException
import io.vertx.mysqlclient.MySQLException
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.exception.PublishedVersionStoreException
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

class MySQLUpsertPublishedVersionHandler(private val publishedUkName: String, private val msg: String) :
    Handler<AsyncResult<RowSet<Row>>> {

    private val logger = LoggerFactory.getLogger(MySQLUpsertPublishedVersionHandler::class.java)

    val future = CompletableFuture<Int>()
    override fun handle(ar: AsyncResult<RowSet<Row>>) {
        if (ar.succeeded()) {
            if (ar.result().rowCount() == 0) {
                future.completeExceptionally(PublishedVersionStoreException("version update rows is 0. $msg"))
                return
            }
            future.complete(ar.result().rowCount())
            return
        }
        val throwable = ar.cause()
        var message = ""
        if (throwable is MySQLBatchException) {
            message = throwable.iterationError.values.firstOrNull()?.message ?: ""
        }
        if (throwable.cause is MySQLBatchException) {
            message = (throwable.cause as MySQLBatchException).iterationError.values.firstOrNull()?.message ?: ""
        }
        if (throwable is MySQLException) {
            message = throwable.message ?: ""
        }
        if (throwable.cause is MySQLException) {
            message = (throwable.cause as MySQLException).message ?: ""
        }
        logger.error("Upsert aggregate published version has exception. {}", msg, throwable)
        if (message.contains(publishedUkName)) {
            future.complete(1)
            return
        }
        if (throwable is MySQLException) {
            future.completeExceptionally(IORuntimeException(msg, throwable))
            return
        }
        future.completeExceptionally(PublishedVersionStoreException(msg, throwable))
        return
    }
}