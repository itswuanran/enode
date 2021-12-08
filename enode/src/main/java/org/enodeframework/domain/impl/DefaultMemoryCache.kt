package org.enodeframework.domain.impl

import org.enodeframework.common.exception.AggregateRootReferenceChangedException
import org.enodeframework.common.exception.AggregateRootTypeNotMatchException
import org.enodeframework.common.scheduling.ScheduleService
import org.enodeframework.common.utils.Assert
import org.enodeframework.domain.AggregateCacheInfo
import org.enodeframework.domain.AggregateRoot
import org.enodeframework.domain.AggregateStorage
import org.enodeframework.domain.MemoryCache
import org.enodeframework.infrastructure.TypeNameProvider
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Consumer

/**
 * @author anruence@gmail.com
 */
class DefaultMemoryCache(
    private val aggregateStorage: AggregateStorage,
    private val scheduleService: ScheduleService,
    private val typeNameProvider: TypeNameProvider
) : MemoryCache {
    private val aggregateRootInfoDict: ConcurrentMap<String, AggregateCacheInfo>
    private val lockObj = Any()
    private val taskName: String
    var timeoutSeconds = 5000
    var scanExpiredAggregateIntervalMilliseconds = 5000
    override fun <T : AggregateRoot> getAsync(
        aggregateRootId: Any,
        aggregateRootType: Class<T>
    ): CompletableFuture<T> {
        Assert.nonNull(aggregateRootId, "aggregateRootId")
        Assert.nonNull(aggregateRootType, "aggregateRootType")
        val future = CompletableFuture<T>()
        val aggregateRootInfo = aggregateRootInfoDict[aggregateRootId.toString()]
        if (aggregateRootInfo == null) {
            future.complete(null)
            return future
        }
        val aggregateRoot = aggregateRootInfo.aggregateRoot as T
        if (aggregateRootInfo.aggregateRoot.javaClass != aggregateRootType) {
            throw AggregateRootTypeNotMatchException(
                String.format(
                    "Incorrect aggregate root type, aggregateRootId:%s, type:%s, expecting type:%s",
                    aggregateRootId,
                    aggregateRootInfo.aggregateRoot.javaClass,
                    aggregateRootType
                )
            )
        }
        if (aggregateRoot.changes.size > 0) {
            val latestAggregateRootFuture = aggregateStorage.getAsync(aggregateRootType, aggregateRootId.toString())
            return latestAggregateRootFuture.thenApply { latestAggregateRoot: AggregateRoot ->
                resetAggregateRootCache(aggregateRootType, aggregateRootId.toString(), latestAggregateRoot)
                latestAggregateRoot as T
            }
        }
        future.complete(aggregateRoot)
        return future
    }

    /**
     * Get an aggregate from memory cache.
     */
    override fun getAsync(aggregateRootId: Any): CompletableFuture<AggregateRoot> {
        return getAsync(aggregateRootId, AggregateRoot::class.java)
    }

    override fun <T : AggregateRoot> acceptAggregateRootChanges(aggregateRoot: T) {
        synchronized(lockObj) {
            val cacheReset = AtomicBoolean(false)
            val cacheInfo = aggregateRootInfoDict.computeIfAbsent(aggregateRoot.uniqueId) {
                aggregateRoot.acceptChanges()
                cacheReset.set(true)
                logger.info(
                    "Aggregate root in-memory cache initialized, aggregateRootType: {}, aggregateRootId: {}, aggregateRootVersion: {}",
                    aggregateRoot.javaClass.name,
                    aggregateRoot.uniqueId,
                    aggregateRoot.version
                )
                AggregateCacheInfo(aggregateRoot)
            }
            if (cacheReset.get()) {
                return
            }
            val aggregateRootOldVersion = cacheInfo!!.aggregateRoot.version
            //更新到内存缓存前需要先检查聚合根引用是否有变化，有变化说明此聚合根已经被重置过状态了
            if (aggregateRoot.version > 1 && cacheInfo.aggregateRoot !== aggregateRoot) {
                throw AggregateRootReferenceChangedException(
                    aggregateRoot
                )
            }
            aggregateRoot.acceptChanges()
            //接受聚合根的最新事件修改，更新聚合根版本号
            cacheInfo.updateAggregateRoot(aggregateRoot)
            logger.info(
                "Aggregate root in-memory cache changed, aggregateRootType: {}, aggregateRootId: {}, aggregateRootNewVersion: {}, aggregateRootOldVersion: {}",
                aggregateRoot.javaClass.name,
                aggregateRoot.uniqueId,
                aggregateRoot.version,
                aggregateRootOldVersion
            )
        }
    }

    override fun <T : AggregateRoot> refreshAggregate(aggregateRoot: T) {
        resetAggregateRootCache(aggregateRoot.javaClass, aggregateRoot.uniqueId, aggregateRoot)
    }

    override fun refreshAggregateFromEventStoreAsync(
        aggregateRootTypeName: String,
        aggregateRootId: String
    ): CompletableFuture<AggregateRoot> {
        Assert.nonNull(aggregateRootTypeName, "aggregateRootTypeName")
        val future = CompletableFuture<AggregateRoot>()
        return try {
            val aggregateRootType = typeNameProvider.getType(aggregateRootTypeName) as Class<AggregateRoot>
            refreshAggregateFromEventStoreAsync(aggregateRootType, aggregateRootId)
        } catch (e: Exception) {
            future.completeExceptionally(e)
            future
        }
    }

    override fun <T : AggregateRoot> refreshAggregateFromEventStoreAsync(
        aggregateRootType: Class<T>,
        aggregateRootId: String
    ): CompletableFuture<T> {
        Assert.nonNull(aggregateRootId, "aggregateRootId")
        Assert.nonNull(aggregateRootType, "aggregateRootType")
        return aggregateStorage.getAsync(aggregateRootType, aggregateRootId).thenApply { aggregateRoot: T ->
            resetAggregateRootCache(aggregateRootType, aggregateRootId, aggregateRoot)
            aggregateRoot
        }.exceptionally { ex: Throwable? ->
            logger.error(
                "Refresh aggregate from event store has unknown exception, aggregateRootTypeName:{}, aggregateRootId:{}",
                typeNameProvider.getTypeName(aggregateRootType),
                aggregateRootId,
                ex
            )
            null
        }
    }

    override fun start() {
        scheduleService.startTask(
            taskName,
            { cleanInactiveAggregateRoot() },
            scanExpiredAggregateIntervalMilliseconds,
            scanExpiredAggregateIntervalMilliseconds
        )
    }

    override fun stop() {
        scheduleService.stopTask(taskName)
    }

    private fun resetAggregateRootCache(
        aggregateRootType: Class<*>,
        aggregateRootId: String,
        aggregateRoot: AggregateRoot
    ) {
        val aggregateCacheInfo = aggregateRootInfoDict.remove(aggregateRootId)
        if (aggregateCacheInfo != null) {
            logger.info(
                "Removed dirty in-memory aggregate, aggregateRootType: {}, aggregateRootId: {}, version: {}",
                aggregateRootType.name,
                aggregateRootId,
                aggregateCacheInfo.aggregateRoot.version
            )
        }
        synchronized(lockObj) {
            val cacheReset = AtomicBoolean(false)
            val cacheInfo = aggregateRootInfoDict.computeIfAbsent(aggregateRoot.uniqueId) {
                if (logger.isDebugEnabled) {
                    logger.debug(
                        "Aggregate root in-memory cache reset, aggregateRootType: {}, aggregateRootId: {}, aggregateRootVersion: {}",
                        aggregateRoot.javaClass.name,
                        aggregateRoot.uniqueId,
                        aggregateRoot.version
                    )
                }
                cacheReset.set(true)
                AggregateCacheInfo(aggregateRoot)
            }
            if (cacheReset.get()) {
                return
            }
            val aggregateRootOldVersion = cacheInfo!!.aggregateRoot.version
            cacheInfo.updateAggregateRoot(aggregateRoot)
            if (logger.isDebugEnabled) {
                logger.debug(
                    "Aggregate root in-memory cache reset, aggregateRootType: {}, aggregateRootId: {}, aggregateRootNewVersion: {}, aggregateRootOldVersion: {}",
                    aggregateRoot.javaClass.name,
                    aggregateRoot.uniqueId,
                    aggregateRoot.version,
                    aggregateRootOldVersion
                )
            }
        }
    }

    private fun cleanInactiveAggregateRoot() {
        val inactiveList: List<Map.Entry<String, AggregateCacheInfo>> = aggregateRootInfoDict.entries
            .filter { entry -> entry.value.isExpired(timeoutSeconds) }
        inactiveList.forEach { entry: Map.Entry<String, AggregateCacheInfo> ->
            if (aggregateRootInfoDict.remove(entry.key) != null) {
                logger.info("Removed inactive aggregate root, id: {}", entry.key)
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultMemoryCache::class.java)
    }

    init {
        aggregateRootInfoDict = ConcurrentHashMap()
        taskName = "CleanInactiveAggregates_" + System.nanoTime() + Random().nextInt(10000)
    }
}