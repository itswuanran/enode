package org.enodeframework.mysql

import io.vertx.mysqlclient.MySQLPool
import io.vertx.sqlclient.Tuple
import org.enodeframework.common.io.IOHelper
import org.enodeframework.common.serializing.SerializeService
import org.enodeframework.configurations.EventStoreConfiguration
import org.enodeframework.eventing.*
import org.enodeframework.mysql.handler.MySQLAddDomainEventsHandler
import org.enodeframework.mysql.handler.MySQLFindDomainEventsHandler
import java.time.ZoneId
import java.util.concurrent.CompletableFuture


/**
 * @author anruence@gmail.com
 */

class MySQLEventStore(
    sqlClient: MySQLPool,
    configuration: EventStoreConfiguration,
    eventSerializer: EventSerializer,
    serializeService: SerializeService
) : EventStore {

    private val eventSerializer: EventSerializer
    private val serializeService: SerializeService
    private val configuration: EventStoreConfiguration
    private val sqlClient: MySQLPool

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
        val sql = String.format(INSERT_EVENT_SQL, configuration.eventTableName)
        val handler = MySQLAddDomainEventsHandler(configuration)
        val tuples = eventStreamList.map { domainEventStream ->
            Tuple.of(
                domainEventStream.aggregateRootId,
                domainEventStream.aggregateRootTypeName,
                domainEventStream.commandId,
                domainEventStream.version,
                domainEventStream.timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                serializeService.serialize(eventSerializer.serialize(domainEventStream.getEvents()))
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
        val handler =
            MySQLFindDomainEventsHandler(eventSerializer, serializeService, "$aggregateRootId#$minVersion#$maxVersion")
        val sql = String.format(SELECT_MANY_BY_VERSION_SQL, configuration.eventTableName)
        val resultSet = sqlClient.preparedQuery(sql).execute(Tuple.of(aggregateRootId, minVersion, maxVersion))
        resultSet.onComplete(handler)
        return handler.future
    }

    override fun findAsync(aggregateRootId: String, version: Int): CompletableFuture<DomainEventStream?> {
        return IOHelper.tryIOFuncAsync({
            findByVersion(aggregateRootId, version)
        }, "FindEventByVersionAsync")
    }

    private fun findByVersion(aggregateRootId: String, version: Int): CompletableFuture<DomainEventStream?> {
        val handler = MySQLFindDomainEventsHandler(eventSerializer, serializeService, "$aggregateRootId#$version")
        val sql = String.format(SELECT_ONE_BY_VERSION_SQL, configuration.eventTableName)
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
        val sql = String.format(SELECT_ONE_BY_COMMAND_ID_SQL, configuration.eventTableName)
        sqlClient.preparedQuery(sql).execute(Tuple.of(aggregateRootId, commandId)).onComplete(handler)
        return handler.future.thenApply { x -> x.firstOrNull() }
    }

    companion object {
        private const val INSERT_EVENT_SQL =
            "INSERT INTO %s (aggregate_root_id, aggregate_root_type_name, command_id, version, gmt_create, events) VALUES (?, ?, ?, ?, ?, ?)"
        private const val SELECT_MANY_BY_VERSION_SQL =
            "SELECT * FROM %s WHERE aggregate_root_id = ? AND version >= ? AND Version <= ? ORDER BY version"
        private const val SELECT_ONE_BY_VERSION_SQL = "SELECT * FROM %s WHERE aggregate_root_id = ? AND version = ?"
        private const val SELECT_ONE_BY_COMMAND_ID_SQL =
            "SELECT * FROM %s WHERE aggregate_root_id = ? AND command_id = ?"
    }

    init {
        this.sqlClient = sqlClient
        this.eventSerializer = eventSerializer
        this.serializeService = serializeService
        this.configuration = configuration
    }
}