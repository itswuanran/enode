package org.enodeframework.mongo.handler

import com.mongodb.MongoServerException
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.exception.PublishedVersionStoreException
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

class MongoFindPublishedVersionHandler(private val msg: String) : Handler<AsyncResult<JsonObject?>> {

    private val logger = LoggerFactory.getLogger(MongoFindPublishedVersionHandler::class.java)

    var future = CompletableFuture<Int>()

    override fun handle(ar: AsyncResult<JsonObject?>) {
        if (ar.succeeded()) {
            future.complete(ar.result()?.getInteger("version", 0) ?: 0)
            return
        }
        val throwable = ar.cause()
        logger.error("Get aggregate published version has exception. {}", msg, throwable)
        if (throwable is MongoServerException) {
            future.completeExceptionally(IORuntimeException(throwable))
            return
        }
        future.completeExceptionally(PublishedVersionStoreException(throwable))
        return
    }
}