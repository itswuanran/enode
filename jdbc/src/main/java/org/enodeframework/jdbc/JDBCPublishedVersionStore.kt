package org.enodeframework.jdbc

import io.vertx.core.AbstractVerticle
import io.vertx.jdbcclient.JDBCPool
import io.vertx.sqlclient.Tuple
import org.enodeframework.common.io.IOHelper
import org.enodeframework.configurations.EventStoreOptions
import org.enodeframework.eventing.PublishedVersionStore
import org.enodeframework.jdbc.handler.JDBCFindPublishedVersionHandler
import org.enodeframework.jdbc.handler.JDBCUpsertPublishedVersionHandler
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import javax.sql.DataSource

/**
 * @author anruence@gmail.com
 */
open class JDBCPublishedVersionStore(
    dataSource: DataSource, private val options: EventStoreOptions
) : AbstractVerticle(), PublishedVersionStore {

    private lateinit var sqlClient: JDBCPool

    private val dataSource: DataSource

    override fun start() {
        super.start()
        this.sqlClient = JDBCPool.pool(vertx, dataSource)
    }

    override fun stop() {
        super.stop()
        this.sqlClient.close()
    }

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
        val handler = JDBCUpsertPublishedVersionHandler(
            options, "$processorName#$aggregateRootTypeName#$aggregateRootId#$publishedVersion"
        )
        val sql = String.format(UPDATE_SQL, options.publishedTableName)
        val tuple = Tuple.of(
            publishedVersion, LocalDateTime.now(), processorName, aggregateRootId, publishedVersion - 1
        )
        sqlClient.preparedQuery(sql).execute(tuple).onComplete(handler)
        return handler.future
    }

    private fun insertVersionAsync(
        processorName: String, aggregateRootTypeName: String, aggregateRootId: String, publishedVersion: Int
    ): CompletableFuture<Int> {
        val handler = JDBCUpsertPublishedVersionHandler(
            options, "$processorName#$aggregateRootTypeName#$aggregateRootId#$publishedVersion"
        )
        val sql = String.format(INSERT_SQL, options.publishedTableName)
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
        val handler = JDBCFindPublishedVersionHandler("$aggregateRootId#$processorName#$aggregateRootTypeName")
        val sql = String.format(SELECT_SQL, options.publishedTableName)
        sqlClient.preparedQuery(sql).execute(Tuple.of(processorName, aggregateRootId)).onComplete(handler)
        return handler.future
    }

    companion object {
        private const val INSERT_SQL =
            "INSERT INTO %s (processor_name, aggregate_root_type_name, aggregate_root_id, version, gmt_create) VALUES (?, ?, ?, ?, ?)"
        private const val UPDATE_SQL =
            "UPDATE %s SET version = ?, gmt_create = ? WHERE processor_name = ? AND aggregate_root_id = ? AND version = ?"
        private const val SELECT_SQL = "SELECT version FROM %s WHERE processor_name = ? AND aggregate_root_id = ?"
    }

    init {
        this.dataSource = dataSource
    }
}