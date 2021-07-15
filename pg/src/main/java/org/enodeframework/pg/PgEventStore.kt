package org.enodeframework.pg

import com.google.common.base.Strings
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.pgclient.PgException
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.Tuple
import org.enodeframework.configurations.EventStoreConfiguration
import org.enodeframework.common.exception.EventStoreException
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.io.IOHelper
import org.enodeframework.common.serializing.ISerializeService
import org.enodeframework.eventing.*
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.regex.Pattern
import java.util.stream.Collectors


/**
 * @author anruence@gmail.com
 */
class PgEventStore(
    client: PgPool,
    configuration: EventStoreConfiguration,
    eventSerializer: IEventSerializer,
    serializeService: ISerializeService
) : IEventStore {

    private val eventSerializer: IEventSerializer
    private val serializeService: ISerializeService
    private val configuration: EventStoreConfiguration
    private val client: PgPool

    private val code: String = "23505"


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
        IOHelper.tryAsyncActionRecursively(
            "BatchAppendAggregateEventsAsync",
            { batchAppendAggregateEvents(aggregateRootId, eventStreamList) },
            { result: AggregateEventAppendResult ->
                batchAggregateEventAppendResult.addCompleteAggregate(
                    aggregateRootId,
                    result
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
        aggregateRootId: String,
        eventStreamList: List<DomainEventStream>
    ): CompletableFuture<AggregateEventAppendResult> {
        val sql = String.format(INSERT_EVENT_SQL, configuration)
        val future = CompletableFuture<AggregateEventAppendResult>()
        val batch: MutableList<Tuple> = ArrayList<Tuple>()
        for (domainEventStream in eventStreamList) {
            batch.add(
                Tuple.of(
                    domainEventStream.aggregateRootId,
                    domainEventStream.aggregateRootTypeName,
                    domainEventStream.commandId,
                    domainEventStream.version,
                    domainEventStream.timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                    serializeService.serialize(eventSerializer.serialize(domainEventStream.events()))
                )
            )
        }
        client.withTransaction { client ->
            client.preparedQuery(sql)
                .executeBatch(batch)
        }.onComplete { ar ->
            if (ar.succeeded()) {
                val appendResult = AggregateEventAppendResult()
                appendResult.eventAppendStatus = EventAppendStatus.Success
                future.complete(appendResult)
                return@onComplete
            }
            val throwable = ar.cause()
            if (throwable is PgException) {
                if (code == throwable.code && throwable.message?.contains(configuration.eventVersionUkName) == true) {
                    val appendResult = AggregateEventAppendResult()
                    appendResult.eventAppendStatus = EventAppendStatus.DuplicateEvent
                    future.complete(appendResult)
                    return@onComplete
                }
                if (code == throwable.code && throwable.message?.contains(configuration.eventCommandIdUkName) == true) {
                    // 不同的数据库在冲突时的错误信息不同，可以通过解析错误信息的方式将冲突的commandId找出来，这里要求id不能命中正则的规则（不包含-字符）
                    val appendResult = AggregateEventAppendResult()
                    appendResult.eventAppendStatus = EventAppendStatus.DuplicateCommand
                    val commandId = this.getDuplicatedId(throwable.message ?: "")
                    if (!Strings.isNullOrEmpty(commandId)) {
                        appendResult.duplicateCommandIds = Lists.newArrayList(commandId)
                    }
                    // 如果没有从异常信息获取到commandId(很低概率获取不到)，需要从db查询出来
                    // 但是Vert.x的线程模型决定了不能再次使用EventLoop线程执行阻塞的查询操作
                    future.complete(appendResult)
                    return@onComplete
                }
                logger.error("Batch append event has sql exception.", throwable)
                future.completeExceptionally(IORuntimeException(throwable))
                return@onComplete
            }
            logger.error("Batch append event has unknown exception.", throwable)
            future.completeExceptionally(EventStoreException(throwable))
            return@onComplete
        }
        return future
    }

    override fun queryAggregateEventsAsync(
        aggregateRootId: String,
        aggregateRootTypeName: String,
        minVersion: Int,
        maxVersion: Int
    ): CompletableFuture<List<DomainEventStream>> {
        return IOHelper.tryIOFuncAsync({
            queryAggregateEvents(aggregateRootId, aggregateRootTypeName, minVersion, maxVersion)
        }, "QueryAggregateEventsAsync")
    }

    private fun queryAggregateEvents(
        aggregateRootId: String,
        aggregateRootTypeName: String,
        minVersion: Int,
        maxVersion: Int
    ): CompletableFuture<List<DomainEventStream>> {
        val future = CompletableFuture<List<DomainEventStream>>()
        val sql = String.format(SELECT_MANY_BY_VERSION_SQL, configuration.eventTableName)
        val resultSet = client.preparedQuery(sql).execute(Tuple.of(aggregateRootId, minVersion, maxVersion))
        resultSet.onComplete { ar ->
            if (ar.succeeded()) {
                future.complete(ar.result().map { row: Row ->
                    this.convertFrom(row.toJson())
                }.toMutableList())
                return@onComplete
            }
            val throwable = ar.cause()
            if (throwable is PgException) {
                val errorMessage = String.format(
                    "Failed to query aggregate events async, aggregateRootId: %s, aggregateRootType: %s",
                    aggregateRootId,
                    aggregateRootTypeName
                )
                logger.error(errorMessage, throwable)
                future.completeExceptionally(IORuntimeException(throwable))
                return@onComplete
            }
            logger.error(
                "Failed to query aggregate events async, aggregateRootId: {}, aggregateRootType: {}",
                aggregateRootId,
                aggregateRootTypeName,
                throwable
            )
            future.completeExceptionally(EventStoreException(throwable))
            return@onComplete
        }
        return future
    }

    override fun findAsync(aggregateRootId: String, version: Int): CompletableFuture<DomainEventStream?> {
        return IOHelper.tryIOFuncAsync({
            findByVersion(aggregateRootId, version)
        }, "FindEventByVersionAsync")
    }

    private fun findByVersion(aggregateRootId: String, version: Int): CompletableFuture<DomainEventStream?> {
        val future = CompletableFuture<DomainEventStream?>()
        val sql = String.format(SELECT_ONE_BY_VERSION_SQL, configuration.eventTableName)
        val array = JsonArray()
        array.add(aggregateRootId)
        array.add(version)
        val rowSet = client.preparedQuery(sql).execute(Tuple.of(aggregateRootId, version))
        rowSet.onComplete { ar ->
            if (ar.succeeded()) {
                ar.result().firstOrNull().let { row: Row? ->
                    future.complete(row?.let { this.convertFrom(it.toJson()) })
                    return@onComplete
                }
            }
            val throwable = ar.cause()
            if (throwable is PgException) {
                logger.error(
                    "Find event by version has sql exception, aggregateRootId: {}, version: {}",
                    aggregateRootId,
                    version,
                    throwable
                )
                future.completeExceptionally(IORuntimeException(throwable))
                return@onComplete
            }
            logger.error(
                "Find event by version has unknown exception, aggregateRootId: {}, version: {}",
                aggregateRootId,
                version,
                throwable
            )
            future.completeExceptionally(EventStoreException(throwable))
            return@onComplete
        }
        return future
    }

    override fun findAsync(aggregateRootId: String, commandId: String): CompletableFuture<DomainEventStream?> {
        return IOHelper.tryIOFuncAsync({
            findByCommandId(aggregateRootId, commandId)
        }, "FindEventByCommandIdAsync")
    }

    private fun findByCommandId(aggregateRootId: String, commandId: String): CompletableFuture<DomainEventStream?> {
        val future = CompletableFuture<DomainEventStream?>()
        val sql = String.format(SELECT_ONE_BY_COMMAND_ID_SQL, configuration.eventTableName)
        client.preparedQuery(sql).execute(Tuple.of(aggregateRootId, commandId)).onComplete { ar ->
            if (ar.succeeded()) {
                ar.result().firstOrNull().let { row: Row? ->
                    future.complete(row?.let { this.convertFrom(it.toJson()) })
                    return@onComplete
                }
            }
            val throwable = ar.cause()
            if (throwable is PgException) {
                logger.error(
                    "Find event by commandId has sql exception, aggregateRootId: {}, commandId: {}",
                    aggregateRootId,
                    commandId,
                    throwable
                )
                future.completeExceptionally(IORuntimeException(throwable))
                return@onComplete
            }
            logger.error(
                "Find event by commandId has unknown exception, aggregateRootId: {}, commandId: {}",
                aggregateRootId,
                commandId,
                throwable
            )
            future.completeExceptionally(EventStoreException(throwable))
            return@onComplete
        }
        return future

    }

    private fun convertFrom(record: JsonObject): DomainEventStream {
        return DomainEventStream(
            record.getString("command_id"),
            record.getString("aggregate_root_id"),
            record.getString("aggregate_root_type_name"),
            Date.from(LocalDateTime.parse(record.getString("gmt_create")).atZone(ZoneId.systemDefault()).toInstant()),
            eventSerializer.deserialize(
                serializeService.deserialize(
                    record.getString("events"),
                    MutableMap::class.java
                ) as MutableMap<String, String>
            ),
            Maps.newHashMap()
        )
    }

    private fun getDuplicatedId(message: String): String {
        val matcher = PATTERN_POSTGRESQL.matcher(message)
        if (!matcher.find()) {
            return ""
        }
        return if (matcher.groupCount() == 0) {
            ""
        } else matcher.group(1)
    }


    companion object {
        private val logger = LoggerFactory.getLogger(PgEventStore::class.java)
        private const val INSERT_EVENT_SQL =
            "INSERT INTO %s (aggregate_root_id, aggregate_root_type_name, command_id, version, gmt_create, events) VALUES (?, ?, ?, ?, ?, ?)"
        private const val SELECT_MANY_BY_VERSION_SQL =
            "SELECT * FROM %s WHERE aggregate_root_id = ? AND version >= ? AND Version <= ? ORDER BY version"
        private const val SELECT_ONE_BY_VERSION_SQL = "SELECT * FROM %s WHERE aggregate_root_id = ? AND version = ?"
        private const val SELECT_ONE_BY_COMMAND_ID_SQL =
            "SELECT * FROM %s WHERE aggregate_root_id = ? AND command_id = ?"
        private val PATTERN_POSTGRESQL = Pattern.compile("=\\(.*, (.*)\\) already exists.$")
    }

    init {
        this.client = client
        this.eventSerializer = eventSerializer
        this.serializeService = serializeService
        this.configuration = configuration
    }
}