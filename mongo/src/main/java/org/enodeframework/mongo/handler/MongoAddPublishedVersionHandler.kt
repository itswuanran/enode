package org.enodeframework.mongo.handler

import com.mongodb.MongoWriteException
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.exception.PublishedVersionStoreException
import org.enodeframework.mongo.MongoEventStore
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

class MongoAddPublishedVersionHandler(
    private val msg: String, private val publishedUkName: String
) : Handler<AsyncResult<String>> {

    private val logger = LoggerFactory.getLogger(MongoEventStore::class.java)

    var future = CompletableFuture<Int>()

    override fun handle(ar: AsyncResult<String>) {
        if (ar.succeeded()) {
            future.complete(1)
            return
        }
        val throwable = ar.cause()
        if (throwable.message?.contains(publishedUkName) == true) {
            future.complete(1)
            return
        }
        logger.error("Insert aggregate published version has unknown exception. {}", msg, throwable)
        if (throwable is MongoWriteException) {
            future.completeExceptionally(IORuntimeException(msg, throwable))
            return
        }
        future.completeExceptionally(PublishedVersionStoreException(msg, throwable))
    }
}