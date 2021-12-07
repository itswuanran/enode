package org.enodeframework.pg

import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.Tuple
import org.enodeframework.common.io.IOHelper
import org.enodeframework.configurations.EventStoreConfiguration
import org.enodeframework.eventing.PublishedVersionStore
import org.enodeframework.pg.handler.PgFindPublishedVersionHandler
import org.enodeframework.pg.handler.PgUpsertPublishedVersionHandler
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

/**
 * @author anruence@gmail.com
 */
class PgPublishedVersionStore(
    private val sqlClient: PgPool, private val configuration: EventStoreConfiguration
) : PublishedVersionStore {

    override fun updatePublishedVersionAsync(
        processorName: String, aggregateRootTypeName: String, aggregateRootId: String, publishedVersion: Int
    ): CompletableFuture<Int> {
        return IOHelper.tryIOFuncAsync({
            updatePublishedVersion(processorName, aggregateRootTypeName, aggregateRootId, publishedVersion)
        }, "UpdatePublishedVersionAsync")
    }

    private fun updatePublishedVersion(
        processorName: String, aggregateRootTypeName: String, aggregateRootId: String, publishedVersion: Int
    ): CompletableFuture<Int> {
        val insert = publishedVersion == 1
        if (insert) {
            return insertVersionAsync(processorName, aggregateRootTypeName, aggregateRootId, publishedVersion)
        }
        return updateVersionAsync(processorName, aggregateRootTypeName, aggregateRootId, publishedVersion)
    }

    private fun updateVersionAsync(
        processorName: String, aggregateRootTypeName: String, aggregateRootId: String, publishedVersion: Int
    ): CompletableFuture<Int> {
        val handler = PgUpsertPublishedVersionHandler(
            configuration.publishedUkName, "$processorName#$aggregateRootTypeName#$aggregateRootId#$publishedVersion"
        )
        val sql = String.format(UPDATE_SQL, configuration.publishedTableName)
        val tuple = Tuple.of(
            publishedVersion, LocalDateTime.now(), processorName, aggregateRootId, publishedVersion - 1
        )
        sqlClient.preparedQuery(sql).execute(tuple).onComplete(handler)
        return handler.future
    }

    private fun insertVersionAsync(
        processorName: String, aggregateRootTypeName: String, aggregateRootId: String, publishedVersion: Int
    ): CompletableFuture<Int> {
        val handler = PgUpsertPublishedVersionHandler(
            configuration.publishedUkName, "$processorName#$aggregateRootTypeName#$aggregateRootId#$publishedVersion"
        )
        val sql = String.format(INSERT_SQL, configuration.publishedTableName)
        val tuple = Tuple.of(processorName, aggregateRootTypeName, aggregateRootId, 1, LocalDateTime.now())
        sqlClient.preparedQuery(sql).execute(tuple).onComplete(handler)
        return handler.future
    }

    override fun getPublishedVersionAsync(
        processorName: String, aggregateRootTypeName: String, aggregateRootId: String
    ): CompletableFuture<Int> {
        return IOHelper.tryIOFuncAsync({
            getPublishedVersion(processorName, aggregateRootTypeName, aggregateRootId)
        }, "UpdatePublishedVersionAsync")
    }

    private fun getPublishedVersion(
        processorName: String, aggregateRootTypeName: String, aggregateRootId: String
    ): CompletableFuture<Int> {
        val handler = PgFindPublishedVersionHandler("$aggregateRootId#$processorName#$aggregateRootTypeName")
        val sql = String.format(SELECT_SQL, configuration.publishedTableName)
        sqlClient.preparedQuery(sql).execute(Tuple.of(processorName, aggregateRootId)).onComplete(handler)
        return handler.future
    }

    companion object {
        private const val INSERT_SQL =
            "INSERT INTO %s (processor_name, aggregate_root_type_name, aggregate_root_id, version, gmt_create) VALUES ($1, $2, $3, $4, $5)"
        private const val UPDATE_SQL =
            "UPDATE %s SET version = $1, gmt_create = $2 WHERE processor_name = $3 AND aggregate_root_id = $4 AND version = $5"
        private const val SELECT_SQL = "SELECT version FROM %s WHERE processor_name = $1 AND aggregate_root_id = $2"
    }
}