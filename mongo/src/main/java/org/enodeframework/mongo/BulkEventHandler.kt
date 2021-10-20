package org.enodeframework.mongo

import com.google.common.base.Strings
import com.google.common.collect.Lists
import com.mongodb.MongoBulkWriteException
import com.mongodb.MongoWriteException
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.ext.mongo.MongoClientBulkWriteResult
import org.enodeframework.common.exception.EventStoreException
import org.enodeframework.configurations.EventStoreConfiguration
import org.enodeframework.eventing.AggregateEventAppendResult
import org.enodeframework.eventing.EventAppendStatus
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.regex.Pattern

class BulkEventHandler(private val configuration: EventStoreConfiguration) :
    Handler<AsyncResult<MongoClientBulkWriteResult>> {

    private val code = 11000

    companion object {
        private val logger = LoggerFactory.getLogger(MongoEventStore::class.java)
        private val PATTERN_MONGO = Pattern.compile("\\{.+?commandId: \"(.+?)\" }$")

    }

    var future = CompletableFuture<AggregateEventAppendResult>()

    override fun handle(ar: AsyncResult<MongoClientBulkWriteResult>) {
        if (ar.succeeded()) {
            val appendResult = AggregateEventAppendResult()
            appendResult.eventAppendStatus = EventAppendStatus.Success
            future.complete(appendResult)
            return
        }
        val throwable = ar.cause()
        var sqlCode = 0
        var message = ""
        if (throwable is MongoWriteException) {
            sqlCode = throwable.code
            message = throwable.message!!
        }
        if (throwable is MongoBulkWriteException) {
            if (throwable.writeErrors.size >= 1) {
                val writeError = throwable.writeErrors[0]
                sqlCode = writeError.code
                message = writeError.message
            }
        }
        if (sqlCode == code && message.contains(configuration.eventVersionUkName)) {
            val appendResult = AggregateEventAppendResult()
            appendResult.eventAppendStatus = EventAppendStatus.DuplicateEvent
            future.complete(appendResult)
            return
        }
        if (sqlCode == code && message.contains(configuration.eventCommandIdUkName)) {
            val appendResult = AggregateEventAppendResult()
            appendResult.eventAppendStatus = EventAppendStatus.DuplicateCommand
            val commandId = getDuplicatedId(message)
            if (!Strings.isNullOrEmpty(commandId)) {
                appendResult.duplicateCommandIds = Lists.newArrayList(commandId)
                future.complete(appendResult)
                return
            }
        }
        logger.error("Batch append event has unknown exception.", throwable)
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