package org.enodeframework.mongo

import QueryVersionHandler
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import org.enodeframework.configurations.EventStoreConfiguration
import org.enodeframework.eventing.IPublishedVersionStore
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author anruence@gmail.com
 */
class MongoPublishedVersionStore @JvmOverloads constructor(
    private val mongoClient: MongoClient,
    private val configuration: EventStoreConfiguration = EventStoreConfiguration.mongo()
) : IPublishedVersionStore {
    override fun updatePublishedVersionAsync(
        processorName: String,
        aggregateRootTypeName: String,
        aggregateRootId: String,
        publishedVersion: Int
    ): CompletableFuture<Int> {
        val insert = publishedVersion == 1
        if (insert) {
            return insertAsync(processorName, aggregateRootTypeName, aggregateRootId, publishedVersion)
        }
        return updateAsync(processorName, aggregateRootTypeName, aggregateRootId, publishedVersion)
    }

    private fun insertAsync(
        processorName: String,
        aggregateRootTypeName: String,
        aggregateRootId: String,
        publishedVersion: Int
    ): CompletableFuture<Int> {
        val document = JsonObject()
        document.put("processorName", processorName)
        document.put("aggregateRootTypeName", aggregateRootTypeName)
        document.put("aggregateRootId", aggregateRootId)
        document.put("version", 1)
        document.put("gmtCreate", Date().toInstant())
        val publishedVersionHandler = InsertPublishedVersionHandler(configuration)
        mongoClient.insert(configuration.publishedTableName, document, publishedVersionHandler)
        return publishedVersionHandler.future
    }

    private fun updateAsync(
        processorName: String,
        aggregateRootTypeName: String,
        aggregateRootId: String,
        publishedVersion: Int
    ): CompletableFuture<Int> {
        val filter = Filters.and(
            Filters.eq("version", publishedVersion - 1),
            Filters.eq("processorName", processorName),
            Filters.eq("aggregateRootId", aggregateRootId)
        )
        val update = Updates.combine(
            Updates.set("version", publishedVersion),
            Updates.set("gmtCreate", Date().toInstant())
        )
        val queryJson = JsonObject(filter.toBsonDocument().toJson())
        val updateJson = JsonObject(update.toBsonDocument().toJson())
        val publishHandle = UpdatePublishedVersionHandler(configuration)
        mongoClient.updateCollection(configuration.publishedTableName, queryJson, updateJson, publishHandle)
        return publishHandle.future
    }

    override fun getPublishedVersionAsync(
        processorName: String,
        aggregateRootTypeName: String,
        aggregateRootId: String
    ): CompletableFuture<Int> {
        val updateFilter = Filters.and(
            Filters.eq("processorName", processorName),
            Filters.eq("aggregateRootId", aggregateRootId)
        )
        val queryJson = JsonObject(updateFilter.toBsonDocument().toJson())
        val queryVersionHandler = QueryVersionHandler()
        mongoClient.findOne(configuration.publishedTableName, queryJson, null, queryVersionHandler)
        return queryVersionHandler.future
    }
}