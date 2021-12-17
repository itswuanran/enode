package org.enodeframework.mongo.handler

import com.mongodb.MongoException
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.ext.mongo.MongoClientUpdateResult
import org.enodeframework.common.exception.EventStoreException
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.mongo.MongoEventStore
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

class MongoUpdatePublishedVersionHandler(private val msg: String) : Handler<AsyncResult<MongoClientUpdateResult?>> {

    companion object {
        private val logger = LoggerFactory.getLogger(MongoEventStore::class.java)
    }

    var future = CompletableFuture<Int>()

    override fun handle(ar: AsyncResult<MongoClientUpdateResult?>) {
        if (ar.succeeded()) {
            future.complete(ar.result()?.docModified?.toInt() ?: 0)
            return
        }
        val throwable = ar.cause()
        if (throwable is MongoException) {
            logger.error("Update aggregate published version has sql exception. {}", msg, throwable)
            future.completeExceptionally(IORuntimeException(msg, throwable))
            return
        }
        logger.error("Update aggregate published version has unknown exception. {}", msg, throwable)
        future.completeExceptionally(EventStoreException(msg, throwable))
        return
    }
}