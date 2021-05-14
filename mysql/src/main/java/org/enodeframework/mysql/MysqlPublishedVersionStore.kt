package org.enodeframework.mysql

import io.vertx.mysqlclient.MySQLException
import io.vertx.mysqlclient.MySQLPool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.Tuple
import org.enodeframework.common.EventStoreConfiguration
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.exception.PublishedVersionStoreException
import org.enodeframework.common.io.IOHelper
import org.enodeframework.eventing.IPublishedVersionStore
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author anruence@gmail.com
 */
class MysqlPublishedVersionStore(private val client: MySQLPool, private val configuration: EventStoreConfiguration) :
    IPublishedVersionStore {

    private val code: String = "23000"

    override fun updatePublishedVersionAsync(
        processorName: String,
        aggregateRootTypeName: String,
        aggregateRootId: String,
        publishedVersion: Int
    ): CompletableFuture<Int> {
        return IOHelper.tryIOFuncAsync({
            updatePublishedVersion(processorName, aggregateRootTypeName, aggregateRootId, publishedVersion)
        }, "UpdatePublishedVersionAsync")
    }

    private fun updatePublishedVersion(
        processorName: String,
        aggregateRootTypeName: String,
        aggregateRootId: String,
        publishedVersion: Int
    ): CompletableFuture<Int> {
        val insert = publishedVersion == 1
        if (insert) {
            return insertVersionAsync(processorName, aggregateRootTypeName, aggregateRootId, publishedVersion)
        }
        return updateVersionAsync(processorName, aggregateRootTypeName, aggregateRootId, publishedVersion)
    }

    private fun updateVersionAsync(
        processorName: String,
        aggregateRootTypeName: String,
        aggregateRootId: String,
        publishedVersion: Int
    ): CompletableFuture<Int> {
        val future = CompletableFuture<Int>()
        val sql = String.format(UPDATE_SQL, configuration.publishedTableName)
        val tuple =
            Tuple.of(publishedVersion, LocalDateTime.now(), processorName, aggregateRootId, publishedVersion - 1)
        client.preparedQuery(sql).execute(tuple).onComplete { ar ->
            if (ar.succeeded()) {
                future.complete(ar.result().rowCount())
                if (ar.result().rowCount() == 0) {
                    future.completeExceptionally(
                        PublishedVersionStoreException(
                            String.format(
                                "version update rows is 0. %s",
                                tuple
                            )
                        )
                    )
                }
                return@onComplete
            }
            val throwable = ar.cause()
            if (throwable is MySQLException) {
                logger.error("Update aggregate published version has sql exception. {}", tuple, throwable)
                future.completeExceptionally(IORuntimeException(throwable))
                return@onComplete
            }
            logger.error("Update aggregate published version has unknown exception. {}", tuple, throwable)
            future.completeExceptionally(PublishedVersionStoreException(throwable))
            return@onComplete
        }
        return future
    }

    private fun insertVersionAsync(
        processorName: String,
        aggregateRootTypeName: String,
        aggregateRootId: String,
        publishedVersion: Int
    ): CompletableFuture<Int> {
        val future = CompletableFuture<Int>()
        val sql = String.format(INSERT_SQL, configuration.publishedTableName)
        val tuple = Tuple.of(processorName, aggregateRootTypeName, aggregateRootId, 1, LocalDateTime.now())
        client.preparedQuery(sql).execute(tuple).onComplete { ar ->
            if (ar.succeeded()) {
                future.complete(ar.result().rowCount())
                return@onComplete
            }
            val throwable = ar.cause()
            if (throwable is MySQLException) {
                if (code == throwable.sqlState && throwable.message?.contains(configuration.publishedUkName) == true) {
                    future.complete(1)
                    return@onComplete
                }
                logger.error("Insert aggregate published version has sql exception. {}", tuple, throwable)
                future.completeExceptionally(IORuntimeException(throwable))
                return@onComplete
            }
            logger.error("Insert aggregate published version has unknown exception. {}", tuple, throwable)
            future.completeExceptionally(PublishedVersionStoreException(throwable))
            return@onComplete
        }
        return future
    }

    override fun getPublishedVersionAsync(
        processorName: String,
        aggregateRootTypeName: String,
        aggregateRootId: String
    ): CompletableFuture<Int> {
        return IOHelper.tryIOFuncAsync({
            getPublishedVersion(processorName, aggregateRootTypeName, aggregateRootId)
        }, "UpdatePublishedVersionAsync")
    }

    private fun getPublishedVersion(
        processorName: String,
        aggregateRootTypeName: String,
        aggregateRootId: String
    ): CompletableFuture<Int> {
        val future = CompletableFuture<Int>()
        val sql = String.format(SELECT_SQL, configuration.publishedTableName)
        client.preparedQuery(sql).execute(Tuple.of(processorName, aggregateRootId)).onComplete { ar ->
            if (ar.succeeded()) {
                ar.result().firstOrNull().let { row: Row? ->
                    if (row == null) {
                        future.complete(0)
                        return@onComplete
                    }
                    future.complete(row.getInteger(0))
                    return@onComplete
                }
            }
            val throwable = ar.cause()
            if (throwable is MySQLException) {
                logger.error("Get aggregate published version has sql exception.", throwable)
                future.completeExceptionally(IORuntimeException(throwable))
                return@onComplete
            }
            logger.error("Get aggregate published version has unknown exception.", throwable)
            future.completeExceptionally(PublishedVersionStoreException(throwable))
            return@onComplete
        }
        return future
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MysqlPublishedVersionStore::class.java)
        private const val INSERT_SQL =
            "INSERT INTO %s (processor_name, aggregate_root_type_name, aggregate_root_id, version, gmt_create) VALUES (?, ?, ?, ?, ?)"
        private const val UPDATE_SQL =
            "UPDATE %s SET version = ?, gmt_create = ? WHERE processor_name = ? AND aggregate_root_id = ? AND version = ?"
        private const val SELECT_SQL = "SELECT version FROM %s WHERE processor_name = ? AND aggregate_root_id = ?"
    }

}