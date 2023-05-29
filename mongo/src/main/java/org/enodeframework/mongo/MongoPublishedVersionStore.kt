package org.enodeframework.mongo

import com.google.common.collect.Lists
import com.mongodb.client.model.Updates
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import org.enodeframework.eventing.EventStoreConfiguration
import org.enodeframework.eventing.PublishedVersionStore
import org.enodeframework.mongo.handler.MongoAddPublishedVersionHandler
import org.enodeframework.mongo.handler.MongoFindPublishedVersionHandler
import org.enodeframework.mongo.handler.MongoUpdatePublishedVersionHandler
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author anruence@gmail.com
 */
open class MongoPublishedVersionStore(
    private val mongoClient: MongoClient, private val options: EventStoreConfiguration
) : PublishedVersionStore {
    override fun updatePublishedVersionAsync(
        processorName: String, aggregateRootTypeName: String, aggregateRootId: String, publishedVersion: Int
    ): CompletableFuture<Int> {
        val insert = publishedVersion == 1
        if (insert) {
            return insertAsync(processorName, aggregateRootTypeName, aggregateRootId, publishedVersion)
        }
        return updateAsync(processorName, aggregateRootTypeName, aggregateRootId, publishedVersion)
    }

    private fun insertAsync(
        processorName: String, aggregateRootTypeName: String, aggregateRootId: String, publishedVersion: Int
    ): CompletableFuture<Int> {
        val document = JsonObject()
        document.put("processorName", processorName)
        document.put("aggregateRootTypeName", aggregateRootTypeName)
        document.put("aggregateRootId", aggregateRootId)
        document.put("version", 1)
        document.put("gmtCreate", Date().toInstant())
        val handler = MongoAddPublishedVersionHandler(
            "$processorName#$aggregateRootTypeName#$aggregateRootId#$publishedVersion", options.publishedUkName
        )
        mongoClient.insert(options.publishedTableName, document, handler)
        return handler.future
    }

    private fun updateAsync(
        processorName: String, aggregateRootTypeName: String, aggregateRootId: String, publishedVersion: Int
    ): CompletableFuture<Int> {
        val map1 = HashMap<String, Int>()
        val map2 = HashMap<String, String>()
        val map3 = HashMap<String, String>()
        map1["version"] = publishedVersion - 1
        map2["aggregateRootId"] = aggregateRootId
        map3["processorName"] = processorName
        val queryJson = JsonObject()
        val queryFilter = Lists.newArrayList(map1, map2, map3)
        queryJson.put("\$and", queryFilter)
        val update = Updates.combine(
            Updates.set("version", publishedVersion), Updates.set("gmtCreate", Date().toInstant())
        )
        val updateJson = JsonObject(update.toBsonDocument().toJson())
        val publishHandle = MongoUpdatePublishedVersionHandler(
            "$processorName#$aggregateRootTypeName#$aggregateRootId#$publishedVersion",
        )
        mongoClient.updateCollection(options.publishedTableName, queryJson, updateJson, publishHandle)
        return publishHandle.future
    }

    override fun getPublishedVersionAsync(
        processorName: String, aggregateRootTypeName: String, aggregateRootId: String
    ): CompletableFuture<Int> {
        val map1 = HashMap<String, String>()
        val map2 = HashMap<String, String>()
        map1["processorName"] = processorName
        map2["aggregateRootId"] = aggregateRootId
        val queryJson = JsonObject()
        val filter = Lists.newArrayList(map1, map2)
        queryJson.put("\$and", filter)
        val handler = MongoFindPublishedVersionHandler("$aggregateRootId#$processorName#$aggregateRootTypeName")
        mongoClient.findOne(options.publishedTableName, queryJson, null, handler)
        return handler.future
    }
}