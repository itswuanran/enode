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

class PgFindPublishedVersionHandler(private val msg: String) : Handler<AsyncResult<RowSet<Row>>> {

    companion object {
        private val logger = LoggerFactory.getLogger(PgFindPublishedVersionHandler::class.java)
    }

    var future = CompletableFuture<Int>()

    override fun handle(ar: AsyncResult<RowSet<Row>>) {
        if (ar.succeeded()) {
            future.complete(ar.result().firstOrNull()?.getInteger(0) ?: 0);
            return
        }
        val throwable = ar.cause()
        logger.error("Get aggregate published version has exception. msg: {}", msg, throwable)
        if (throwable is PgException) {
            future.completeExceptionally(IORuntimeException(msg, throwable))
            return
        }
        future.completeExceptionally(PublishedVersionStoreException(msg, throwable))
        return
    }
}
