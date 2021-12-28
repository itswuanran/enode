package org.enodeframework.mysql

import io.vertx.mysqlclient.MySQLPool
import io.vertx.sqlclient.Tuple
import org.enodeframework.common.io.IOHelper
import org.enodeframework.common.serializing.SerializeService
import org.enodeframework.configurations.EventStoreOptions
import org.enodeframework.eventing.*
import org.enodeframework.mysql.handler.MySQLAddDomainEventsHandler
import org.enodeframework.mysql.handler.MySQLFindDomainEventsHandler
import java.time.ZoneId
import java.util.concurrent.CompletableFuture


/**
 * @author anruence@gmail.com
 */

open class MySQLEventStore(
    sqlClient: MySQLPool,
    configuration: EventStoreOptions,
    eventSerializer: EventSerializer,
    serializeService: SerializeService
) : EventStore {

    private val eventSerializer: EventSerializer
    private val serializeService: SerializeService
    private val options: EventStoreOptions
    private val sqlClient: MySQLPool
    private val num: Int = 200

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
        IOHelper.tryAsyncActionRecursively(
            "BatchAppendAggregateEventsAsync",
            { batchAppendAggregateEvents(aggregateRootId, eventStreamList) },
            { result: AggregateEventAppendResult ->
                batchAggregateEventAppendResult.addCompleteAggregate(
                    aggregateRootId, result
                )
            },
            {
                String.format("[aggregateRootId: %s, eventStreamCount: %s]", aggregateRootId, eventStreamList.size)
            },
            null,
            retryTimes,
            true
        )
    }

    private fun batchAppendAggregateEvents(
        aggregateRootId: String, eventStreamList: List<DomainEventStream>
    ): CompletableFuture<AggregateEventAppendResult> {
        val sql = String.format(INSERT_EVENT_SQL, options.eventTableName)
        val handler = MySQLAddDomainEventsHandler(options, aggregateRootId)
        val tuples = eventStreamList.map { domainEventStream ->
            Tuple.of(
                domainEventStream.aggregateRootId,
                domainEventStream.aggregateRootTypeName,
                domainEventStream.commandId,
                domainEventStream.version,
                domainEventStream.timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                serializeService.serialize(eventSerializer.serialize(domainEventStream.events))
            )
        }
        sqlClient.withTransaction { client ->
            client.preparedQuery(sql).executeBatch(tuples).onComplete(handler)
        }
        return handler.future
    }

    override fun queryAggregateEventsAsync(
        aggregateRootId: String, aggregateRootTypeName: String, minVersion: Int, maxVersion: Int
    ): CompletableFuture<List<DomainEventStream>> {
        return IOHelper.tryIOFuncAsync({
            queryAggregateEvents(aggregateRootId, aggregateRootTypeName, minVersion, maxVersion)
        }, "QueryAggregateEventsAsync")
    }

    private fun queryAggregateEvents(
        aggregateRootId: String, aggregateRootTypeName: String, minVersion: Int, maxVersion: Int
    ): CompletableFuture<List<DomainEventStream>> {
        val stepVersion = (minVersion + num - 1).coerceAtMost(maxVersion)
        return queryAggregateEventsInBatches(
            aggregateRootId, aggregateRootTypeName, minVersion, stepVersion, minVersion, maxVersion
        )
    }

    private fun queryAggregateEventsInBatches(
        aggregateRootId: String,
        aggregateRootTypeName: String,
        currMinVersion: Int,
        currMaxVersion: Int,
        minVersion: Int,
        maxVersion: Int
    ): CompletableFuture<List<DomainEventStream>> {
        val handler = MySQLFindDomainEventsHandler(
            eventSerializer, serializeService, "$aggregateRootId#$currMinVersion#$currMaxVersion"
        )
        val sql = String.format(SELECT_MANY_BY_VERSION_SQL, options.eventTableName)
        val resultSet = sqlClient.preparedQuery(sql).execute(Tuple.of(aggregateRootId, currMinVersion, currMaxVersion))
        resultSet.onComplete(handler)
        return handler.future.thenCompose { eventStreams ->
            eventStreamHandleAsync(
                eventStreams, aggregateRootId, aggregateRootTypeName, currMaxVersion, minVersion, maxVersion
            )
        }.thenApply { values -> values.sortedBy { y -> y.version } }
    }

    private fun eventStreamHandleAsync(
        domainEventStreams: List<DomainEventStream>,
        aggregateRootId: String,
        aggregateRootTypeName: String,
        currMaxVersion: Int,
        minVersion: Int,
        maxVersion: Int
    ): CompletableFuture<List<DomainEventStream>> {
        // 一般有多大版本，召回的数量就有多少，如果不相等说明已经召回到最后一页
        if (domainEventStreams.size < currMaxVersion - minVersion + 1 || currMaxVersion == maxVersion) {
            return CompletableFuture.completedFuture(domainEventStreams)
        }
        return queryAggregateEventsInBatches(
            aggregateRootId, aggregateRootTypeName, currMaxVersion + 1, currMaxVersion + num, minVersion, maxVersion
        ).thenApply { values ->
            if (values == null || values.isEmpty()) {
                return@thenApply domainEventStreams
            }
            values.toMutableList().addAll(domainEventStreams)
            return@thenApply values
        }
    }

    override fun findAsync(aggregateRootId: String, version: Int): CompletableFuture<DomainEventStream?> {
        return IOHelper.tryIOFuncAsync({
            findByVersion(aggregateRootId, version)
        }, "FindEventByVersionAsync")
    }

    private fun findByVersion(aggregateRootId: String, version: Int): CompletableFuture<DomainEventStream?> {
        val handler = MySQLFindDomainEventsHandler(eventSerializer, serializeService, "$aggregateRootId#$version")
        val sql = String.format(SELECT_ONE_BY_VERSION_SQL, options.eventTableName)
        sqlClient.preparedQuery(sql).execute(Tuple.of(aggregateRootId, version)).onComplete(handler)
        return handler.future.thenApply { x -> x.firstOrNull() }
    }

    override fun findAsync(aggregateRootId: String, commandId: String): CompletableFuture<DomainEventStream?> {
        return IOHelper.tryIOFuncAsync({
            findByCommandId(aggregateRootId, commandId)
        }, "FindEventByCommandIdAsync")
    }

    private fun findByCommandId(aggregateRootId: String, commandId: String): CompletableFuture<DomainEventStream?> {
        val handler = MySQLFindDomainEventsHandler(eventSerializer, serializeService, "$aggregateRootId#$commandId")
        val sql = String.format(SELECT_ONE_BY_COMMAND_ID_SQL, options.eventTableName)
        sqlClient.preparedQuery(sql).execute(Tuple.of(aggregateRootId, commandId)).onComplete(handler)
        return handler.future.thenApply { x -> x.firstOrNull() }
    }

    companion object {
        private const val INSERT_EVENT_SQL =
            "INSERT INTO %s (aggregate_root_id, aggregate_root_type_name, command_id, version, gmt_create, events) VALUES (?, ?, ?, ?, ?, ?)"
        private const val SELECT_MANY_BY_VERSION_SQL =
            "SELECT * FROM %s WHERE aggregate_root_id = ? AND version >= ? AND version <= ? ORDER BY version"
        private const val SELECT_ONE_BY_VERSION_SQL = "SELECT * FROM %s WHERE aggregate_root_id = ? AND version = ?"
        private const val SELECT_ONE_BY_COMMAND_ID_SQL =
            "SELECT * FROM %s WHERE aggregate_root_id = ? AND command_id = ?"
    }

    init {
        this.sqlClient = sqlClient
        this.eventSerializer = eventSerializer
        this.serializeService = serializeService
        this.options = configuration
    }
}