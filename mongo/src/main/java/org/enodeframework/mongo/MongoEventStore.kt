package org.enodeframework.mongo

import FindEventHandler
import com.google.common.collect.Lists
import com.mongodb.client.model.Filters
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.BulkOperation
import io.vertx.ext.mongo.MongoClient
import org.enodeframework.common.io.IOHelper.tryAsyncActionRecursively
import org.enodeframework.common.io.IOHelper.tryIOFuncAsync
import org.enodeframework.common.serializing.ISerializeService
import org.enodeframework.configurations.EventStoreConfiguration
import org.enodeframework.eventing.*
import java.util.concurrent.CompletableFuture
import java.util.stream.Collectors

/**
 * @author anruence@gmail.com
 */
class MongoEventStore(
    private val mongoClient: MongoClient,
    private val configuration: EventStoreConfiguration,
    private val eventSerializer: IEventSerializer,
    private val serializeService: ISerializeService
) : IEventStore {
    constructor(
        mongoClient: MongoClient,
        eventSerializer: IEventSerializer,
        serializeService: ISerializeService
    ) : this(mongoClient, EventStoreConfiguration.mongo(), eventSerializer, serializeService)

    override fun batchAppendAsync(eventStreams: List<DomainEventStream>): CompletableFuture<EventAppendResult> {
        val future = CompletableFuture<EventAppendResult>()
        val appendResult = EventAppendResult()
        if (eventStreams.isEmpty()) {
            future.complete(appendResult)
            return future
        }
        val eventStreamMap = eventStreams.stream().distinct()
            .collect(Collectors.groupingBy { obj: DomainEventStream -> obj.aggregateRootId })
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
                    aggregateRootId,
                    result
                )
            },
            { String.format("[aggregateRootId: %s, eventStreamCount: %s]", aggregateRootId, eventStreamList.size) },
            null,
            retryTimes, true
        )
    }

    private fun tryFindEventByCommandIdAsync(
        aggregateRootId: String,
        commandId: String,
        duplicateCommandIds: MutableList<String>,
        retryTimes: Int
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
            retryTimes, true
        )
        return future
    }

    private fun batchAppendAggregateEventsAsync(
        aggregateRootId: String,
        eventStreamList: List<DomainEventStream>
    ): CompletableFuture<AggregateEventAppendResult> {
        val promise = BulkEventHandler(configuration)
        val bulks: MutableList<BulkOperation> = Lists.newArrayList()
        for (domainEventStream in eventStreamList) {
            val document = JsonObject()
            document.put("aggregateRootId", domainEventStream.aggregateRootId)
            document.put("aggregateRootTypeName", domainEventStream.aggregateRootTypeName)
            document.put("commandId", domainEventStream.commandId)
            document.put("version", domainEventStream.version)
            document.put("gmtCreate", domainEventStream.timestamp.toInstant())
            document.put("events", serializeService.serialize(eventSerializer.serialize(domainEventStream.events())))
            val bulk = BulkOperation.createInsert(document)
            bulks.add(bulk)
        }
        mongoClient.bulkWrite(configuration.eventTableName, bulks).onComplete(promise)
        return promise.future
    }

    override fun queryAggregateEventsAsync(
        aggregateRootId: String,
        aggregateRootTypeName: String,
        minVersion: Int,
        maxVersion: Int
    ): CompletableFuture<List<DomainEventStream>> {
        return tryIOFuncAsync({
            val filter = Filters.and(
                Filters.eq("aggregateRootId", aggregateRootId),
                Filters.gte("version", minVersion),
                Filters.lte("version", maxVersion)
            )
            val jsonObject = JsonObject(filter.toBsonDocument().toJson())
            val msg = String.format("%s#%s#%s#%s", aggregateRootId, aggregateRootTypeName, minVersion, maxVersion)
            val manyEventHandler = FindManyEventHandler(eventSerializer, serializeService, msg)
            mongoClient.find(configuration.eventTableName, jsonObject, manyEventHandler)
            manyEventHandler.future
        }, "QueryAggregateEventsAsync")
    }

    override fun findAsync(aggregateRootId: String, version: Int): CompletableFuture<DomainEventStream?> {
        return tryIOFuncAsync({
            val queryBson = Filters.and(Filters.eq("aggregateRootId", aggregateRootId), Filters.eq("version", version))
            val query = JsonObject(queryBson.toBsonDocument().toJson())
            val msg = String.format("%s#%s", aggregateRootId, version)
            val findEventHandler = FindEventHandler(eventSerializer, serializeService, msg)
            mongoClient.findOne(configuration.eventTableName, query, null, findEventHandler)
            findEventHandler.future
        }, "FindEventByVersionAsync")
    }

    override fun findAsync(aggregateRootId: String, commandId: String): CompletableFuture<DomainEventStream?> {
        return tryIOFuncAsync({
            val query = Filters.and(
                Filters.eq("aggregateRootId", aggregateRootId),
                Filters.eq("commandId", commandId)
            )
            val queryJson = JsonObject(query.toBsonDocument().toJson())
            val msg = String.format("%s#%s", aggregateRootId, commandId)
            val findEventHandler = FindEventHandler(eventSerializer, serializeService, msg)
            mongoClient.findOne(configuration.eventTableName, queryJson, null, findEventHandler)
            findEventHandler.future
        }, "FindEventByCommandIdAsync")
    }
}