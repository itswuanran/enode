package org.enodeframework.mongo

import com.mongodb.MongoWriteException
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import org.enodeframework.common.exception.EventStoreException
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.configurations.EventStoreConfiguration
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

class InsertPublishedVersionHandler(private val configuration: EventStoreConfiguration) : Handler<AsyncResult<String>> {

    private val code = 11000

    companion object {
        private val logger = LoggerFactory.getLogger(MongoEventStore::class.java)
    }

    var future = CompletableFuture<Int>()

    override fun handle(ar: AsyncResult<String>) {
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
        if (throwable is MongoWriteException) {
            if (throwable.code == code && throwable.message?.contains(configuration.publishedUkName) == true) {
                future.complete(1)
                return
            }
            logger.error("Insert or update aggregate published version has sql exception.", throwable)
            future.completeExceptionally(IORuntimeException(throwable))
            return
        }
        logger.error("Insert or update aggregate published version has unknown exception.", throwable)
        future.completeExceptionally(EventStoreException(throwable))
        return
    }
}