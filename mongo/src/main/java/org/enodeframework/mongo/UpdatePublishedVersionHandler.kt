package org.enodeframework.mongo

import com.mongodb.MongoServerException
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.ext.mongo.MongoClientUpdateResult
import org.enodeframework.common.exception.EventStoreException
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.configurations.EventStoreConfiguration
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.regex.Pattern

class UpdatePublishedVersionHandler(private val configuration: EventStoreConfiguration) :
    Handler<AsyncResult<MongoClientUpdateResult>> {

    private val code = 11000

    companion object {
        private val logger = LoggerFactory.getLogger(MongoEventStore::class.java)
        private val PATTERN_MONGO = Pattern.compile("\\{.+?commandId: \"(.+?)\" }$")

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

    /**
     * E11000 duplicate key error collection: enode.event_stream index: aggregateRootId_1_commandId_1 dup key: { aggregateRootId: "5ee8b610d7671114741829c7", commandId: "5ee8b61bd7671114741829cf" }
     */
    private fun getDuplicatedId(message: String): String {
        val matcher = PATTERN_MONGO.matcher(message)
        if (matcher.find()) {
            if (matcher.groupCount() == 1) {
                return matcher.group(1)
            }
        }
        return ""
    }
}