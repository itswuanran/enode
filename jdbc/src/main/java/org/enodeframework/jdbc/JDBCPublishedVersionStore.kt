package org.enodeframework.jdbc

import io.vertx.core.json.JsonArray
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.SQLClient
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.ext.sql.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.exception.PublishedVersionStoreException
import org.enodeframework.common.io.IOHelperAwait
import org.enodeframework.common.utilities.Ensure
import org.enodeframework.eventing.IPublishedVersionStore
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.sql.DataSource

/**
 * @author anruence@gmail.com
 */
open class JDBCPublishedVersionStore @JvmOverloads constructor(dataSource: DataSource, setting: DBConfiguration = DBConfiguration()) : CoroutineVerticle(), IPublishedVersionStore {
    private val tableName: String
    private val uniqueIndexName: String
    private val sqlState: String
    private val dataSource: DataSource
    private lateinit var sqlClient: SQLClient

    override suspend fun start() {
        sqlClient = JDBCClient.create(vertx, dataSource)
    }

    override fun updatePublishedVersionAsync(processorName: String, aggregateRootTypeName: String, aggregateRootId: String, publishedVersion: Int): CompletableFuture<Int> {
        return IOHelperAwait.tryIOFuncAsync({
            CoroutineScope(Dispatchers.Default).async { updatePublishedVersion(processorName, aggregateRootTypeName, aggregateRootId, publishedVersion) }
        }, "UpdatePublishedVersionAsync");
    }

    private suspend fun updatePublishedVersion(processorName: String, aggregateRootTypeName: String, aggregateRootId: String, publishedVersion: Int): Int {
        val isUpdate = publishedVersion != 1
        val sql = if (isUpdate) String.format(UPDATE_SQL, tableName) else String.format(INSERT_SQL, tableName)
        val array = JsonArray()
        if (isUpdate) {
            array.add(publishedVersion)
            array.add(Date().toInstant())
            array.add(processorName)
            array.add(aggregateRootId)
            array.add(publishedVersion - 1)
        } else {
            array.add(processorName)
            array.add(aggregateRootTypeName)
            array.add(aggregateRootId)
            array.add(1)
            array.add(Date().toInstant())
        }
        try {
            val updateResult = sqlClient.updateWithParamsAwait(sql, array)
            if (updateResult.updated == 0) {
                throw PublishedVersionStoreException(String.format("version update rows is 0. %s", array))
            }
            return updateResult.updated
        } catch (throwable: Throwable) {
            if (throwable is SQLException) {
                if (!isUpdate && sqlState == throwable.sqlState && throwable.message!!.contains(uniqueIndexName)) {
                    return 1
                }
                logger.error("Insert or update aggregate published version has sql exception. {}", array, throwable)
                throw IORuntimeException(throwable)
            }
            logger.error("Insert or update aggregate published version has unknown exception. {}", array, throwable)
            throw PublishedVersionStoreException(throwable)
        }
    }

    override fun getPublishedVersionAsync(processorName: String, aggregateRootTypeName: String, aggregateRootId: String): CompletableFuture<Int> {
        return IOHelperAwait.tryIOFuncAsync({
            CoroutineScope(Dispatchers.Default).async { getPublishedVersion(processorName, aggregateRootTypeName, aggregateRootId) }
        }, "UpdatePublishedVersionAsync");
    }

    private suspend fun getPublishedVersion(processorName: String, aggregateRootTypeName: String, aggregateRootId: String): Int {
        val sql = String.format(SELECT_SQL, tableName)
        val array = JsonArray()
        array.add(processorName)
        array.add(aggregateRootId)
        try {
            val result = sqlClient.querySingleWithParamsAwait(sql, array) ?: return 0
            return result.getInteger(0)
        } catch (throwable: Throwable) {
            if (throwable is SQLException) {
                logger.error("Get aggregate published version has sql exception.", throwable)
                throw IORuntimeException(throwable)
            }
            logger.error("Get aggregate published version has unknown exception.", throwable)
            throw PublishedVersionStoreException(throwable)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(JDBCPublishedVersionStore::class.java)
        private const val INSERT_SQL = "INSERT INTO %s (processor_name, aggregate_root_type_name, aggregate_root_id, version, gmt_create) VALUES (?, ?, ?, ?, ?)"
        private const val UPDATE_SQL = "UPDATE %s SET version = ?, gmt_create = ? WHERE processor_name = ? AND aggregate_root_id = ? AND version = ?"
        private const val SELECT_SQL = "SELECT version FROM %s WHERE processor_name = ? AND aggregate_root_id = ?"
    }

    init {
        Ensure.notNull(dataSource, "DataSource")
        Ensure.notNull(dataSource, "DBConfigurationSetting")
        this.dataSource = dataSource
        tableName = setting.publishedVersionTableName
        sqlState = setting.sqlState
        uniqueIndexName = setting.publishedVersionUniqueIndexName
    }
}