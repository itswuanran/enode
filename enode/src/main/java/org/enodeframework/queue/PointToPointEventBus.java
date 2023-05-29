package org.enodeframework.queue;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.ConnectionBase;
import io.vertx.ext.eventbus.bridge.tcp.impl.protocol.FrameHelper;
import org.enodeframework.common.exception.ReplyAddressInvalidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * An event bus implementation that point to point with other Vert.x nodes
 */
public class PointToPointEventBus {

    private static final Logger log = LoggerFactory.getLogger(PointToPointEventBus.class);

    private final EventBusOptions options;

    private final NetClient client;

    private final Vertx vertx;

    private final ConcurrentMap<String, ConnectionHolder> connections = new ConcurrentHashMap<>();

    public PointToPointEventBus(Vertx vertx, VertxOptions options) {
        this.vertx = vertx;
        this.options = options.getEventBusOptions();
        this.client = vertx.createNetClient(new NetClientOptions(this.options.toJson()));
    }

    NetClient client() {
        return client;
    }

    public void send(String address, JsonObject message) {
        OutboundDeliveryContext ctx = new OutboundDeliveryContext(message);
        sendToNode(ctx, address);
    }

    public void close() {
        if (client != null) {
            client.close(serverClose -> {
                if (serverClose.failed()) {
                    log.error("Failed to close server", serverClose.cause());
                }
                // Close all outbound connections explicitly - don't rely on context hooks
                for (ConnectionHolder holder : connections.values()) {
                    holder.close();
                }
            });
        }
    }

    private void sendToNode(OutboundDeliveryContext sendContext, String nodeId) {
        sendRemote(sendContext, nodeId);
    }

    private void sendRemote(OutboundDeliveryContext sendContext, String remoteNodeId) {
        // We need to deal with the fact that connecting can take some time and is async, and we cannot
        // block to wait for it. So we add any sends to a pending list if not connected yet.
        // Once we connect we send them.
        // This can also be invoked concurrently from different threads, so it gets a little
        // tricky
        ConnectionHolder holder = connections.get(remoteNodeId);
        if (holder == null) {
            // When process is creating a lot of connections this can take some time
            // so increase the timeout
            holder = new ConnectionHolder(this, remoteNodeId);
            ConnectionHolder prevHolder = connections.putIfAbsent(remoteNodeId, holder);
            if (prevHolder != null) {
                // Another one sneaked in
                holder = prevHolder;
            } else {
                holder.connect();
            }
        }
        holder.writeMessage(sendContext);
    }

    ConcurrentMap<String, ConnectionHolder> connections() {
        return connections;
    }

    EventBusOptions options() {
        return options;
    }

    Vertx vertx() {
        return vertx;
    }
}

class OutboundDeliveryContext implements Handler<AsyncResult<Void>> {

    public final JsonObject message;

    public OutboundDeliveryContext(JsonObject message) {
        this.message = message;
    }

    @Override
    public void handle(AsyncResult<Void> event) {

    }

    public void written(Throwable cause) {

    }

}

class ConnectionHolder {

    private final Logger log = LoggerFactory.getLogger(ConnectionHolder.class);

    private final PointToPointEventBus eventBus;

    private final String remoteNodeAddress;

    private Queue<OutboundDeliveryContext> pending;

    private NetSocket socket;

    private boolean connected;

    private long timeoutID = -1;

    private long pingTimeoutID = -1;

    public ConnectionHolder(PointToPointEventBus eventBus, String remoteNodeAddress) {
        this.eventBus = eventBus;
        this.remoteNodeAddress = remoteNodeAddress;
    }

    SocketAddress toURI(String value) {
        try {
            URI uri = new URI(value);
            return SocketAddress.inetSocketAddress(uri.getPort(), uri.getHost());
        } catch (Exception e) {
            log.error("parse address error. uri: {}", value, e);
            throw new ReplyAddressInvalidException(value, e);
        }
    }

    void connect() {
        SocketAddress socketAddress = toURI(remoteNodeAddress);
        eventBus.client().connect(socketAddress)
            .onComplete(ar -> {
                if (ar.succeeded()) {
                    connected(ar.result());
                } else {
                    log.warn("Connecting to server " + remoteNodeAddress + " failed", ar.cause());
                    close(ar.cause());
                }
            });
    }

    // TODO optimise this (contention on monitor)
    synchronized void writeMessage(OutboundDeliveryContext ctx) {
        if (connected) {
            FrameHelper.sendFrame("send", remoteNodeAddress, ctx.message, socket);
        } else {
            if (pending == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Not connected to server " + remoteNodeAddress + " - starting queuing");
                }
                pending = new ArrayDeque<>();
            }
            pending.add(ctx);
        }
    }

    void close() {
        close(ConnectionBase.CLOSED_EXCEPTION);
    }

    private void close(Throwable cause) {
        if (timeoutID != -1) {
            eventBus.vertx().cancelTimer(timeoutID);
        }
        if (pingTimeoutID != -1) {
            eventBus.vertx().cancelTimer(pingTimeoutID);
        }
        synchronized (this) {
            OutboundDeliveryContext msg;
            if (pending != null) {
                while ((msg = pending.poll()) != null) {
                    msg.written(cause);
                    log.error("connection closed, queue msg: {}", msg, cause);
                }
            }
        }
        // The holder can be null or different if the target server is restarted with same nodeInfo
        // before the cleanup for the previous one has been processed
        if (eventBus.connections().remove(remoteNodeAddress, this)) {
            if (log.isDebugEnabled()) {
                log.debug("Point to point connection closed for server " + remoteNodeAddress);
            }
        }
    }

    private void schedulePing() {
        EventBusOptions options = eventBus.options();
        pingTimeoutID = eventBus.vertx().setTimer(options.getClusterPingInterval(), id1 -> {
            // If we don't get a pong back in time we close the connection
            timeoutID = eventBus.vertx().setTimer(options.getClusterPingReplyInterval(), id2 -> {
                // Didn't get pong in time - consider connection dead
                log.warn("No pong from server " + remoteNodeAddress + " - will consider it dead");
                close();
            });
            FrameHelper.sendFrame("ping", remoteNodeAddress, new JsonObject(), socket);
        });
    }

    private synchronized void connected(NetSocket socket) {
        this.socket = socket;
        connected = true;
        socket.exceptionHandler(err -> {
            close(err);
        });
        socket.closeHandler(v -> close());
        socket.handler(data -> {
            // Got a pong back
            eventBus.vertx().cancelTimer(timeoutID);
            schedulePing();
        });
        // Start a pinger
        schedulePing();
        if (pending != null) {
            if (log.isDebugEnabled()) {
                log.debug("Draining the queue for server " + remoteNodeAddress);
            }
            for (OutboundDeliveryContext ctx : pending) {
                FrameHelper.sendFrame("send", remoteNodeAddress, ctx.message, socket);
            }
        }
        pending = null;
    }
}