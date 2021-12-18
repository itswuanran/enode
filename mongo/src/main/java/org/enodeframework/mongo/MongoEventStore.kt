package org.enodeframework.mongo

import com.google.common.collect.Lists
import com.mongodb.client.model.Filters
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.BulkOperation
import io.vertx.ext.mongo.MongoClient
import org.enodeframework.common.io.IOHelper.tryAsyncActionRecursively
import org.enodeframework.common.io.IOHelper.tryIOFuncAsync
import org.enodeframework.common.serializing.SerializeService
import org.enodeframework.configurations.EventStoreOptions
import org.enodeframework.eventing.*
import org.enodeframework.mongo.handler.MongoAddDomainEventsHandler
import org.enodeframework.mongo.handler.MongoFindDomainEventsHandler
import java.util.concurrent.CompletableFuture

/**
 * @author anruence@gmail.com
 */
open class MongoEventStore(
    private val mongoClient: MongoClient,
    private val options: EventStoreOptions,
    private val eventSerializer: EventSerializer,
    private val serializeService: SerializeService
) : EventStore {
    constructor(
        mongoClient: MongoClient, eventSerializer: EventSerializer, serializeService: SerializeService
    ) : this(mongoClient, EventStoreOptions.mongo(), eventSerializer, serializeService)

    override fun batchAppendAsync(eventStreams: List<DomainEventStream>): CompletableFuture<EventAppendResult> {
        val future = CompletableFuture<EventAppendResult>()
        val appendResult = EventAppendResult()
        if (eventStreams.isEmpty()) {
            future.complete(appendResult)
            return future
        }
        val eventStreamMap = eventStreams.distinct().groupBy { obj: DomainEventStream -> obj.aggregateRootId }
        val batchAggregateEventAppendResult = BatchAggregateEventAppendResult(eventStreamMap.keys.size)
        for ((key, value) in eventStreamMap) {
            batchAppendAggregateEventsAsync(key, value, batchAggregateEventAppendResult, 0)
        }
        return batchAggregateEventAppendResult.taskCompletionSource
    }

    private fun batchAppendAggregateEventsAsync(
        aggregateRootId: String,
        eventStreamList: List<DomainEventStream>,
        batchAggregateEventAppendResult: BatchAggregateEventAppendResult,
        retryTimes: Int
    ) {
        tryAsyncActionRecursively(
            "BatchAppendAggregateEventsAsync",
            { batchAppendAggregateEventsAsync(aggregateRootId, eventStreamList) },
            { result: AggregateEventAppendResult? ->
                batchAggregateEventAppendResult.addCompleteAggregate(
                    aggregateRootId, result
                )
            },
            { String.format("[aggregateRootId: %s, eventStreamCount: %s]", aggregateRootId, eventStreamList.size) },
            null,
            retryTimes,
            true
        )
    }

    private fun tryFindEventByCommandIdAsync(
        aggregateRootId: String, commandId: String, duplicateCommandIds: MutableList<String>, retryTimes: Int
    ): CompletableFuture<DomainEventStream?> {
        val future = CompletableFuture<DomainEventStream?>()
        tryAsyncActionRecursively(
            "TryFindEventByCommandIdAsync",
            { findAsync(aggregateRootId, commandId) },
            { result: DomainEventStream? ->
                if (result != null) {
                    duplicateCommandIds.add(result.commandId)
                }
                future.complete(result)
            },
            { String.format("[aggregateRootId:%s, commandId:%s]", aggregateRootId, commandId) },
            null,
            retryTimes,
            true
        )
        return future
    }

    private fun batchAppendAggregateEventsAsync(
        aggregateRootId: String, eventStreamList: List<DomainEventStream>
    ): CompletableFuture<AggregateEventAppendResult> {
        val handler = MongoAddDomainEventsHandler(options, aggregateRootId)
        val bulks: MutableList<BulkOperation> = Lists.newArrayList()
        for (domainEventStream in eventStreamList) {
            val document = JsonObject()
            document.put("aggregateRootId", domainEventStream.aggregateRootId)
            document.put("aggregateRootTypeName", domainEventStream.aggregateRootTypeName)
            document.put("commandId", domainEventStream.commandId)
            document.put("version", domainEventStream.version)
            document.put("gmtCreate", domainEventStream.timestamp.toInstant())
            document.put("events", serializeService.serialize(eventSerializer.serialize(domainEventStream.getEvents())))
            val bulk = BulkOperation.createInsert(document)
            bulks.add(bulk)
        }
        mongoClient.bulkWrite(options.eventTableName, bulks).onComplete(handler)
        return handler.future
    }

    override fun queryAggregateEventsAsync(
        aggregateRootId: String, aggregateRootTypeName: String, minVersion: Int, maxVersion: Int
    ): CompletableFuture<List<DomainEventStream>> {
        return tryIOFuncAsync({
            val filter = Filters.and(
                Filters.eq("aggregateRootId", aggregateRootId),
                Filters.gte("version", minVersion),
                Filters.lte("version", maxVersion)
            )
            val jsonObject = JsonObject(filter.toBsonDocument().toJson())
            val msg = String.format("%s#%s#%s#%s", aggregateRootId, aggregateRootTypeName, minVersion, maxVersion)
            val manyEventHandler = MongoFindDomainEventsHandler(eventSerializer, serializeService, msg)
            mongoClient.find(options.eventTableName, jsonObject, manyEventHandler)
            manyEventHandler.future
        }, "QueryAggregateEventsAsync")
    }

    override fun findAsync(aggregateRootId: String, version: Int): CompletableFuture<DomainEventStream?> {
        return tryIOFuncAsync({
            val map1 = HashMap<String, String>()
            val map2 = HashMap<String, Int>()
            map1["aggregateRootId"] = aggregateRootId
            map2["version"] = version
            val queryJson = JsonObject()
            val queryFilter = Lists.newArrayList(map1, map2)
            queryJson.put("\$and", queryFilter)
            val findEventHandler =
                MongoFindDomainEventsHandler(eventSerializer, serializeService, "$aggregateRootId#$version")
            mongoClient.find(options.eventTableName, queryJson, findEventHandler)
            findEventHandler.future.thenApply { x -> x.firstOrNull() }
        }, "FindEventByVersionAsync")
    }

    override fun findAsync(aggregateRootId: String, commandId: String): CompletableFuture<DomainEventStream?> {
        return tryIOFuncAsync({
            val map1 = HashMap<String, String>()
            val map2 = HashMap<String, String>()
            map1["aggregateRootId"] = aggregateRootId
            map2["commandId"] = commandId
            val queryJson = JsonObject()
            val queryFilter = Lists.newArrayList(map1, map2)
            queryJson.put("\$and", queryFilter)
            val findEventHandler =
                MongoFindDomainEventsHandler(eventSerializer, serializeService, "$aggregateRootId#$commandId")
            mongoClient.find(options.eventTableName, queryJson, findEventHandler)
            findEventHandler.future.thenApply { x -> x.firstOrNull() }
        }, "FindEventByCommandIdAsync")
    }
}