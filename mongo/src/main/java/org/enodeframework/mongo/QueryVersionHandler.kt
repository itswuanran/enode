import com.mongodb.MongoServerException
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.exception.PublishedVersionStoreException
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

class QueryVersionHandler : Handler<AsyncResult<JsonObject>> {

    companion object {
        private val logger = LoggerFactory.getLogger(QueryVersionHandler::class.java)
    }

    var future = CompletableFuture<Int>()

    override fun handle(ar: AsyncResult<JsonObject>) {
        if (ar.succeeded()) {
            val result = ar.result()
            if (result == null) {
                future.complete(0)
                return
            }
            val version = result.getInteger("version", 0)
            future.complete(version)
            return
        }
        val throwable = ar.cause()
        if (throwable is MongoServerException) {
            logger.error(
                "Get aggregate published version has sql exception. aggregateRootId: {}",
                "aggregateRootId",
                throwable
            )
            future.completeExceptionally(IORuntimeException(throwable))
            return
        }
        logger.error(
            "Get aggregate published version has unknown exception. aggregateRootId: {}",
            "aggregateRootId",
            throwable
        )
        future.completeExceptionally(PublishedVersionStoreException(throwable))
        return
    }
}