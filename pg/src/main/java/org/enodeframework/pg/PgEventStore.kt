package org.enodeframework.pg

import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.Tuple
import org.enodeframework.common.io.IOHelper
import org.enodeframework.common.serializing.SerializeService
import org.enodeframework.eventing.AggregateEventAppendResult
import org.enodeframework.eventing.BatchAggregateEventAppendResult
import org.enodeframework.eventing.DomainEventStream
import org.enodeframework.eventing.EventAppendResult
import org.enodeframework.eventing.EventSerializer
import org.enodeframework.eventing.EventStore
import org.enodeframework.eventing.EventStoreOptions
import java.util.concurrent.CompletableFuture

/**
 * @author anruence@gmail.com
 */
open class PgEventStore(
    pgPool: PgPool,
    options: EventStoreOptions,
    eventSerializer: EventSerializer,
    serializeService: SerializeService
) : EventStore {

    private val eventSerializer: EventSerializer
    private val serializeService: SerializeService
    private val options: EventStoreOptions
    private val pgPool: PgPool

    override fun batchAppendAsync(eventStreams: List<DomainEventStream>): CompletableFuture<EventAppendResult> {
        if (eventStreams.isEmpty()) {
            return CompletableFuture.completedFuture(EventAppendResult())
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
                "[aggregateRootId: $aggregateRootId, eventStreamCount: ${eventStreamList.size}]"
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
        val handler = PgAddDomainEventsHandler(options, aggregateRootId)
        val tuples = eventStreamList.map { domainEventStream ->
            Tuple.of(
                domainEventStream.aggregateRootId,
                domainEventStream.aggregateRootTypeName,
                domainEventStream.commandId,
                domainEventStream.version,
                serializeService.serialize(eventSerializer.serialize(domainEventStream.events)),
                domainEventStream.timestamp.time
            )
        }
        pgPool.withTransaction { client ->
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
            PgFindDomainEventsHandler(eventSerializer, serializeService, "$aggregateRootId#$minVersion#$maxVersion")
        val sql = String.format(SELECT_MANY_BY_VERSION_SQL, options.eventTableName)
        val resultSet = pgPool.preparedQuery(sql).execute(Tuple.of(aggregateRootId, minVersion, maxVersion))
        resultSet.onComplete(handler)
        return handler.future
    }

    override fun findAsync(aggregateRootId: String, version: Int): CompletableFuture<DomainEventStream?> {
        return IOHelper.tryIOFuncAsync({
            findByVersion(aggregateRootId, version)
        }, "FindEventByVersionAsync")
    }

    private fun findByVersion(aggregateRootId: String, version: Int): CompletableFuture<DomainEventStream?> {
        val handler = PgFindDomainEventsHandler(eventSerializer, serializeService, "$aggregateRootId#$version")
        val sql = String.format(SELECT_ONE_BY_VERSION_SQL, options.eventTableName)
        pgPool.preparedQuery(sql).execute(Tuple.of(aggregateRootId, version)).onComplete(handler)
        return handler.future.thenApply { x -> x.firstOrNull() }
    }

    override fun findAsync(aggregateRootId: String, commandId: String): CompletableFuture<DomainEventStream?> {
        return IOHelper.tryIOFuncAsync({
            findByCommandId(aggregateRootId, commandId)
        }, "FindEventByCommandIdAsync")
    }

    private fun findByCommandId(aggregateRootId: String, commandId: String): CompletableFuture<DomainEventStream?> {
        val handler = PgFindDomainEventsHandler(eventSerializer, serializeService, "$aggregateRootId#$commandId")
        val sql = String.format(SELECT_ONE_BY_COMMAND_ID_SQL, options.eventTableName)
        pgPool.preparedQuery(sql).execute(Tuple.of(aggregateRootId, commandId)).onComplete(handler)
        return handler.future.thenApply { x -> x.firstOrNull() }
    }

    companion object {
        private const val INSERT_EVENT_SQL =
            "INSERT INTO %s (aggregate_root_id, aggregate_root_type_name, command_id, version, events, create_at) VALUES ($1, $2, $3, $4, $5, $6)"
        private const val SELECT_MANY_BY_VERSION_SQL =
            "SELECT * FROM %s WHERE aggregate_root_id = $1 AND version >= $2 AND version <= $3 ORDER BY version ASC"
        private const val SELECT_ONE_BY_VERSION_SQL = "SELECT * FROM %s WHERE aggregate_root_id = $1 AND version = $2"
        private const val SELECT_ONE_BY_COMMAND_ID_SQL =
            "SELECT * FROM %s WHERE aggregate_root_id = $1 AND command_id = $2"
    }

    init {
        this.pgPool = pgPool
        this.eventSerializer = eventSerializer
        this.serializeService = serializeService
        this.options = options
    }
}