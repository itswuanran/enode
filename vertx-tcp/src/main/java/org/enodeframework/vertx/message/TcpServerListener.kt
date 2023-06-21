package org.enodeframework.vertx.message

import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.core.net.NetServerOptions
import io.vertx.ext.bridge.BridgeOptions
import io.vertx.ext.bridge.PermittedOptions
import io.vertx.ext.eventbus.bridge.tcp.TcpEventBusBridge
import org.enodeframework.queue.command.CommandResultProcessor
import org.enodeframework.queue.reply.GenericReplyMessage
import org.slf4j.LoggerFactory

/**
 * @author anruence@gmail.com
 */
class TcpServerListener(
    private val commandResultProcessor: CommandResultProcessor,
    private val serverOptions: NetServerOptions,
) : AbstractVerticle() {

    private lateinit var tcpEventBusBridge: TcpEventBusBridge

    private val logger = LoggerFactory.getLogger(TcpServerListener::class.java)

    override fun start() {
        val address = commandResultProcessor.ReplyAddress()
        vertx.eventBus().consumer(address) { msg: Message<JsonObject> ->
            processRequestInternal(msg.body())
        }
        val bridgeOptions = BridgeOptions()
        bridgeOptions.addInboundPermitted(PermittedOptions().setAddress(address))
        bridgeOptions.addOutboundPermitted(PermittedOptions().setAddress(address))
        tcpEventBusBridge = TcpEventBusBridge.create(vertx, bridgeOptions, serverOptions)
        tcpEventBusBridge.listen(serverOptions.port).onComplete { res ->
            if (!res.succeeded()) {
                logger.error("vertx netServer start failed. addr: {}", address, res.cause())
            }
        }
    }

    override fun stop() {
        tcpEventBusBridge.close()
    }

    private fun processRequestInternal(reply: JsonObject) {
        val data = reply.getJsonObject("data")
        commandResultProcessor.processReplyMessage(data.mapTo(GenericReplyMessage::class.java))
    }
}