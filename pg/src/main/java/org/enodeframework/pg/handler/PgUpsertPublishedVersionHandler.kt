package org.enodeframework.pg.handler

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.pgclient.PgException
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.exception.PublishedVersionStoreException
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

class PgUpsertPublishedVersionHandler(private val publishedUkName: String, private val msg: String) :
    Handler<AsyncResult<RowSet<Row>>> {

    companion object {
        private val logger = LoggerFactory.getLogger(PgUpsertPublishedVersionHandler::class.java)
    }

    val future = CompletableFuture<Int>()

    override fun handle(ar: AsyncResult<RowSet<Row>>) {
        if (ar.succeeded()) {
            if (ar.result().rowCount() == 0) {
                future.completeExceptionally(
                    PublishedVersionStoreException(String.format("version update rows is 0. %s", msg))
                )
                return
            }
            future.complete(ar.result().rowCount())
            return
        }
        val ex = ar.cause()
        var throwable = ex
        if (ex is PgException) {
            throwable = ex;
        }
        if (ex.cause is PgException) {
            throwable = ex.cause
        }
        if (throwable.message?.contains(publishedUkName) == true) {
            future.complete(1)
            return
        }
        logger.error("Upsert aggregate published version has exception. {}", msg, throwable)
        if (throwable is PgException) {
            future.completeExceptionally(IORuntimeException(msg, throwable))
            return
        }
        future.completeExceptionally(PublishedVersionStoreException(msg, throwable))
        return
    }
}