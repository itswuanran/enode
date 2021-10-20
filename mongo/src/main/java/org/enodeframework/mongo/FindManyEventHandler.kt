package org.enodeframework.mongo

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.mongodb.MongoServerException
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import org.enodeframework.common.exception.EventStoreException
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.serializing.ISerializeService
import org.enodeframework.eventing.DomainEventStream
import org.enodeframework.eventing.IEventSerializer
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture

class FindManyEventHandler(
    private val eventSerializer: IEventSerializer,
    private val serializeService: ISerializeService,
    private val msg: String
) :
    Handler<AsyncResult<List<JsonObject>>> {

    companion object {
        private val logger = LoggerFactory.getLogger(MongoEventStore::class.java)

    }

    var future = CompletableFuture<List<DomainEventStream>>()

    override fun handle(ar: AsyncResult<List<JsonObject>>) {
        if (ar.succeeded()) {
            val streams: MutableList<DomainEventStream> = Lists.newArrayList()
            val documents = ar.result()
            documents.forEach { document ->
                val eventStream = DomainEventStream(
                    document.getString("commandId"),
                    document.getString("aggregateRootId"),
                    document.getString("aggregateRootTypeName"),
                    Date(document.getInstant("gmtCreate").toEpochMilli()),
                    eventSerializer.deserialize(
                        (serializeService.deserialize(
                            document.getString("events"),
                            MutableMap::class.java
                        ) as MutableMap<String, String>)
                    ),
                    Maps.newHashMap()
                )
                streams.add(eventStream)
            }
            streams.sortWith(Comparator.comparingInt { obj: DomainEventStream -> obj.version })
            future.complete(streams)
            return
        }
        val throwable = ar.cause()
        if (throwable is MongoServerException) {
            val errorMessage = String.format(
                "Failed to query aggregate events async, aggregateRootId: %s",
                msg,
            )
            logger.error(errorMessage, throwable)
            future.completeExceptionally(IORuntimeException(throwable))
            return
        }
        logger.error(
            "Failed to query aggregate events async, aggregateRootId: {}",
            msg,
            throwable
        )
        future.completeExceptionally(EventStoreException(throwable))
        return
    }
}