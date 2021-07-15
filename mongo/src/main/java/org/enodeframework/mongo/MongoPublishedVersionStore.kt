package org.enodeframework.mongo

import com.mongodb.MongoWriteException
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.client.result.InsertOneResult
import com.mongodb.client.result.UpdateResult
import com.mongodb.reactivestreams.client.MongoClient
import org.bson.Document
import org.enodeframework.configurations.EventStoreConfiguration
import org.enodeframework.common.exception.EventStoreException
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.exception.PublishedVersionStoreException
import org.enodeframework.eventing.IPublishedVersionStore
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author anruence@gmail.com
 */
class MongoPublishedVersionStore @JvmOverloads constructor(
    private val mongoClient: MongoClient,
    private val configuration: EventStoreConfiguration = EventStoreConfiguration.mongo()
) : IPublishedVersionStore {
    private val code = 11000
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
        val future = CompletableFuture<Int>()
        val document = Document()
        document["processorName"] = processorName
        document["aggregateRootTypeName"] = aggregateRootTypeName
        document["aggregateRootId"] = aggregateRootId
        document["version"] = 1
        document["gmtCreate"] = Date()
        mongoClient.getDatabase(configuration.dbName).getCollection(configuration.publishedTableName)
            .insertOne(document).subscribe(object : Subscriber<InsertOneResult> {
                override fun onSubscribe(s: Subscription) {
                    s.request(1)
                }

                override fun onNext(x: InsertOneResult) {
                    future.complete(if (x.wasAcknowledged()) 1 else 0)
                }

                override fun onError(throwable: Throwable) {

                    if (throwable is MongoWriteException) {
                        if (throwable.code == code && throwable.message?.contains(configuration.publishedUkName) == true) {
                            future.complete(1)
                            return
                        }
                        logger.error("Insert or update aggregate published version has sql exception.", throwable)
                        future.completeExceptionally(IORuntimeException(throwable))
                        return
                    }
                    logger.error("Insert or update aggregate published version has unknown exception.", throwable)
                    future.completeExceptionally(EventStoreException(throwable))
                    return
                }

                override fun onComplete() {
                    future.complete(1)
                }
            })
        return future
    }

    private fun updateAsync(
        processorName: String,
        aggregateRootTypeName: String,
        aggregateRootId: String,
        publishedVersion: Int
    ): CompletableFuture<Int> {

        val future = CompletableFuture<Int>()
        val filter = Filters.and(
            Filters.eq("version", publishedVersion - 1),
            Filters.eq("processorName", processorName),
            Filters.eq("aggregateRootId", aggregateRootId)
        )
        val update = Updates.combine(
            Updates.set("version", publishedVersion),
            Updates.set("gmtCreate", Date())
        )
        mongoClient.getDatabase(configuration.dbName).getCollection(configuration.publishedTableName)
            .updateOne(filter, update).subscribe(object : Subscriber<UpdateResult> {

                private var updated = 0

                override fun onSubscribe(s: Subscription) {
                    s.request(1)
                }

                override fun onNext(x: UpdateResult) {
                    updated = x.modifiedCount.toInt()
                }

                override fun onError(throwable: Throwable) {
                    if (throwable is MongoWriteException) {
                        logger.error("Update aggregate published version has sql exception.", throwable)
                        future.completeExceptionally(IORuntimeException(throwable))
                        return
                    }
                    logger.error("Update aggregate published version has unknown exception.", throwable)
                    future.completeExceptionally(EventStoreException(throwable))
                    return
                }

                override fun onComplete() {
                    future.complete(updated)
                    return
                }
            })
        return future
    }

    override fun getPublishedVersionAsync(
        processorName: String,
        aggregateRootTypeName: String,
        aggregateRootId: String
    ): CompletableFuture<Int> {
        val future = CompletableFuture<Int>()
        val updateFilter = Filters.and(
            Filters.eq("processorName", processorName),
            Filters.eq("aggregateRootId", aggregateRootId)
        )
        mongoClient.getDatabase(configuration.dbName).getCollection(configuration.publishedTableName)
            .find(updateFilter).subscribe(object : Subscriber<Document> {
                private var version = 0
                override fun onSubscribe(s: Subscription) {
                    s.request(1)
                }

                override fun onNext(document: Document) {
                    version = document.getInteger("version")
                    future.complete(version)
                }

                override fun onError(throwable: Throwable) {
                    if (throwable is MongoWriteException) {
                        logger.error(
                            "Get aggregate published version has sql exception. aggregateRootId: {}",
                            aggregateRootId,
                            throwable
                        )
                        future.completeExceptionally(IORuntimeException(throwable))
                        return
                    }
                    logger.error(
                        "Get aggregate published version has unknown exception. aggregateRootId: {}",
                        aggregateRootId,
                        throwable
                    )
                    future.completeExceptionally(PublishedVersionStoreException(throwable))
                    return
                }

                override fun onComplete() {
                    future.complete(version)
                    return
                }
            })
        return future
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MongoPublishedVersionStore::class.java)
    }
}