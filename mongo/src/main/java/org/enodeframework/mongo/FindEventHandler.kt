import com.google.common.collect.Maps
import com.mongodb.MongoServerException
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import org.enodeframework.common.exception.EventStoreException
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.serializing.ISerializeService
import org.enodeframework.eventing.DomainEventStream
import org.enodeframework.eventing.IEventSerializer
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture

class FindEventHandler(
    private val eventSerializer: IEventSerializer,
    private val serializeService: ISerializeService,
    private val msg: String
) : Handler<AsyncResult<JsonObject>> {

    companion object {
        private val logger = LoggerFactory.getLogger(FindEventHandler::class.java)
    }

    var future = CompletableFuture<DomainEventStream?>()

    override fun handle(ar: AsyncResult<JsonObject>) {
        if (ar.succeeded()) {
            val document = ar.result()
            if (document == null) {
                future.complete(null)
                return
            }
            val eventStream = DomainEventStream(
                document.getString("commandId"),
                document.getString("aggregateRootId"),
                document.getString("aggregateRootTypeName"),
                Date(document.getInstant("gmtCreate").toEpochMilli()),
                eventSerializer.deserialize(
                    (serializeService.deserialize(
                        document.getString("events"),
                        MutableMap::class.java
                    ) as MutableMap<String, String>)
                ),
                Maps.newHashMap()
            )
            future.complete(eventStream)
            return
        }
        val throwable = ar.cause()
        if (throwable is MongoServerException) {
            val errorMessage = String.format(
                "Failed to query aggregate events async, aggregateRootId: %s",
                msg
            )
            logger.error(errorMessage, throwable)
            future.completeExceptionally(IORuntimeException(throwable))
            return
        }
        logger.error(
            "Failed to query aggregate events async, aggregateRootId: {}",
            msg,
            throwable
        )
        future.completeExceptionally(EventStoreException(throwable))
        return
    }
}