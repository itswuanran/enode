package org.enodeframework.mongo

import com.google.common.base.Strings
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.mongodb.MongoBulkWriteException
import com.mongodb.MongoServerException
import com.mongodb.MongoWriteException
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClientBulkWriteResult
import io.vertx.ext.mongo.MongoClientUpdateResult
import org.enodeframework.common.exception.EventStoreException
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.exception.PublishedVersionStoreException
import org.enodeframework.common.serializing.SerializeService
import org.enodeframework.eventing.*
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture

class MongoAddDomainEventsHandler(
    private val options: EventStoreOptions,
    private val msg: String,
) : Handler<AsyncResult<MongoClientBulkWriteResult>> {

    private val logger = LoggerFactory.getLogger(MongoAddDomainEventsHandler::class.java)

    val future = CompletableFuture<AggregateEventAppendResult>()

    override fun handle(ar: AsyncResult<MongoClientBulkWriteResult>) {
        if (ar.succeeded()) {
            val appendResult = AggregateEventAppendResult(EventAppendStatus.Success)
            future.complete(appendResult)
            return
        }
        val throwable = ar.cause()
        var message = ""
        if (throwable is MongoWriteException) {
            message = throwable.message!!
        }
        if (throwable is MongoBulkWriteException) {
            message = throwable.writeErrors.firstOrNull()?.message ?: ""
        }
        if (message.contains(options.eventVersionUkName)) {
            val appendResult = AggregateEventAppendResult(EventAppendStatus.DuplicateEvent)
            future.complete(appendResult)
            return
        }
        if (message.contains(options.eventCommandIdUkName)) {
            val appendResult = AggregateEventAppendResult(EventAppendStatus.DuplicateCommand)
            val commandId = options.seekCommandId(message)
            if (!Strings.isNullOrEmpty(commandId)) {
                appendResult.duplicateCommandIds = Lists.newArrayList(commandId)
            } else {
                appendResult.duplicateCommandIds = Lists.newArrayList(message)
            }
            future.complete(appendResult)
            return
        }
        logger.error("Batch append event has exception. {}", msg, throwable)
        if (throwable is MongoServerException) {
            future.completeExceptionally(IORuntimeException(msg, throwable))
        }
        future.completeExceptionally(EventStoreException(msg, throwable))
        return
    }
}

class MongoAddPublishedVersionHandler(
    private val msg: String, private val publishedUkName: String
) : Handler<AsyncResult<String>> {

    private val logger = LoggerFactory.getLogger(MongoAddPublishedVersionHandler::class.java)

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
        return
    }
}

class MongoFindDomainEventsHandler(
    private val eventSerializer: EventSerializer,
    private val serializeService: SerializeService,
    private val msg: String
) : Handler<AsyncResult<List<JsonObject>>> {

    private val logger = LoggerFactory.getLogger(MongoFindDomainEventsHandler::class.java)

    val future = CompletableFuture<List<DomainEventStream>>()

    override fun handle(ar: AsyncResult<List<JsonObject>>) {
        if (ar.succeeded()) {
            val documents = ar.result()
            val streams = documents.map { document ->
                DomainEventStream(
                    document.getString("commandId"),
                    document.getString("aggregateRootId"),
                    document.getString("aggregateRootTypeName"),
                    Date(document.getLong("createAt")),
                    eventSerializer.deserialize(
                        (serializeService.deserialize(
                            document.getString("events"), MutableMap::class.java
                        ) as MutableMap<String, String>)
                    ),
                    Maps.newHashMap()
                )
            }.toMutableList()
            streams.sortWith(Comparator.comparingInt { obj: DomainEventStream -> obj.version })
            future.complete(streams)
            return
        }
        val throwable = ar.cause()
        if (throwable is MongoServerException) {
            logger.error("Failed to query aggregate events async, msg: {}", msg, throwable)
            future.completeExceptionally(IORuntimeException(msg, throwable))
            return
        }
        logger.error("Failed to query aggregate events async, msg: {}", msg, throwable)
        future.completeExceptionally(EventStoreException(msg, throwable))
        return
    }
}

class MongoFindPublishedVersionHandler(private val msg: String) : Handler<AsyncResult<JsonObject?>> {

    private val logger = LoggerFactory.getLogger(MongoFindPublishedVersionHandler::class.java)

    var future = CompletableFuture<Int>()

    override fun handle(ar: AsyncResult<JsonObject?>) {
        if (ar.succeeded()) {
            future.complete(ar.result()?.getInteger("version", 0) ?: 0)
            return
        }
        val throwable = ar.cause()
        logger.error("Find aggregate published version has exception. {}", msg, throwable)
        if (throwable is MongoServerException) {
            future.completeExceptionally(IORuntimeException(throwable))
            return
        }
        future.completeExceptionally(PublishedVersionStoreException(throwable))
        return
    }
}

class MongoUpdatePublishedVersionHandler(private val msg: String) : Handler<AsyncResult<MongoClientUpdateResult?>> {

    private val logger = LoggerFactory.getLogger(MongoUpdatePublishedVersionHandler::class.java)

    var future = CompletableFuture<Int>()

    override fun handle(ar: AsyncResult<MongoClientUpdateResult?>) {
        if (ar.succeeded()) {
            future.complete(ar.result()?.docModified?.toInt() ?: 0)
            return
        }
        val throwable = ar.cause()
        logger.error("Update aggregate published version has exception. {}", msg, throwable)
        if (throwable is MongoServerException) {
            future.completeExceptionally(IORuntimeException(msg, throwable))
            return
        }
        future.completeExceptionally(EventStoreException(msg, throwable))
        return
    }
}