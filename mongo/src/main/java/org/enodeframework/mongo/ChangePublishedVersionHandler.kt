package org.enodeframework.mongo

import com.mongodb.MongoServerException
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.ext.mongo.MongoClientUpdateResult
import org.enodeframework.common.exception.EventStoreException
import org.enodeframework.common.exception.IORuntimeException
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

class ChangePublishedVersionHandler : Handler<AsyncResult<MongoClientUpdateResult>> {

    companion object {
        private val logger = LoggerFactory.getLogger(MongoEventStore::class.java)
    }

    var future = CompletableFuture<Int>()

    override fun handle(ar: AsyncResult<MongoClientUpdateResult>) {
        if (ar.succeeded()) {
            val result = ar.result()
            if (result == null) {
                future.complete(0)
                return
            }
            future.complete(1)
            return
        }
        val throwable = ar.cause()
        if (throwable is MongoServerException) {
            logger.error("Insert or update aggregate published version has sql exception.", throwable)
            future.completeExceptionally(IORuntimeException(throwable))
            return
        }
        logger.error("Insert or update aggregate published version has unknown exception.", throwable)
        future.completeExceptionally(EventStoreException(throwable))
        return
    }
}