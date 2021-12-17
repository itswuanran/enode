package org.enodeframework.jdbc.handler

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.exception.PublishedVersionStoreException
import org.enodeframework.configurations.DbType
import org.enodeframework.configurations.EventStoreConfiguration
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.util.concurrent.CompletableFuture

class JDBCUpsertPublishedVersionHandler(
    private val configuration: EventStoreConfiguration,
    private val msg: String
) :
    Handler<AsyncResult<RowSet<Row>>> {

    companion object {
        private val logger = LoggerFactory.getLogger(JDBCUpsertPublishedVersionHandler::class.java)
    }

    private var code: String = ""

    init {
        if (DbType.MySQL.name == configuration.dbType) {
            this.code = "23000"
        }
        if (DbType.Pg.name == configuration.dbType) {
            this.code = "23505"
        }
    }

    var future = CompletableFuture<Int>()

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
        if (throwable is SQLException) {
            if (code == throwable.sqlState && throwable.message?.contains(configuration.publishedUkName) == true) {
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