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

    private val code: String = "23505"

    var future = CompletableFuture<Int>()

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
        val throwable = ar.cause()
        if (throwable is PgException) {
            if (code == throwable.code && throwable.message?.contains(publishedUkName) == true) {
                future.complete(1)
                return
            }
            logger.error("Upsert aggregate published version has sql exception. {}", msg, throwable)
            future.completeExceptionally(IORuntimeException(msg, throwable))
            return
        }
        logger.error("Upsert aggregate published version has unknown exception. {}", msg, throwable)
        future.completeExceptionally(PublishedVersionStoreException(msg, throwable))
        return
    }
}