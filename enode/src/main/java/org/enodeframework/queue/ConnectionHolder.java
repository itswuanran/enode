package org.enodeframework.queue;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.eventbus.impl.OutboundDeliveryContext;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.ConnectionBase;
import io.vertx.ext.eventbus.bridge.tcp.impl.protocol.FrameHelper;
import org.enodeframework.common.exception.ReplyAddressException;
import org.enodeframework.common.utils.ReplyUtil;

import java.util.ArrayDeque;
import java.util.Queue;

class ConnectionHolder {

    private static final Logger log = LoggerFactory.getLogger(ConnectionHolder.class);
    private final PointToPointEventBus eventBus;
    private final String remoteNodeId;
    private final Vertx vertx;
    private Queue<OutboundDeliveryContext<?>> pending;
    private NetSocket socket;
    private boolean connected;
    private long timeoutID = -1;
    private long pingTimeoutID = -1;

    ConnectionHolder(PointToPointEventBus eventBus, String remoteNodeId) {
        this.eventBus = eventBus;
        this.remoteNodeId = remoteNodeId;
        this.vertx = eventBus.vertx();
    }

    void connect() {
        SocketAddress info = ReplyUtil.toSocketAddress(remoteNodeId);
        if (info == null) {
            throw new ReplyAddressException("");
        }
        eventBus.client().connect(info.port(), info.host()).onComplete(ar -> {
            if (ar.succeeded()) {
                connected(ar.result());
            } else {
                log.warn("Connecting to server " + remoteNodeId + " failed", ar.cause());
                close(ar.cause());
            }
        });
    }

    // TODO optimise this (contention on monitor)
    synchronized void writeMessage(OutboundDeliveryContext<?> ctx) {
        if (connected) {
            FrameHelper.sendFrame("send", remoteNodeId, ctx.message, socket);
        } else {
            if (pending == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Not connected to server " + remoteNodeId + " - starting queuing");
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
            vertx.cancelTimer(timeoutID);
        }
        if (pingTimeoutID != -1) {
            vertx.cancelTimer(pingTimeoutID);
        }
        synchronized (this) {
            OutboundDeliveryContext<?> msg;
            if (pending != null) {
                while ((msg = pending.poll()) != null) {
                    msg.written(cause);
                }
            }
        }
        // The holder can be null or different if the target server is restarted with same nodeInfo
        // before the cleanup for the previous one has been processed
        if (eventBus.connections().remove(remoteNodeId, this)) {
            if (log.isDebugEnabled()) {
                log.debug("Cluster connection closed for server " + remoteNodeId);
            }
        }
    }

    private void schedulePing() {
        EventBusOptions options = eventBus.options();
        pingTimeoutID = vertx.setTimer(options.getClusterPingInterval(), id1 -> {
            // If we don't get a pong back in time we close the connection
            timeoutID = vertx.setTimer(options.getClusterPingReplyInterval(), id2 -> {
                // Didn't get pong in time - consider connection dead
                log.warn("No pong from server " + remoteNodeId + " - will consider it dead");
                close();
            });
            FrameHelper.sendFrame("ping", remoteNodeId, new JsonObject(), socket);
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
            vertx.cancelTimer(timeoutID);
            schedulePing();
        });
        // Start a pinger
        schedulePing();
        if (pending != null) {
            if (log.isDebugEnabled()) {
                log.debug("Draining the queue for server " + remoteNodeId);
            }
            for (OutboundDeliveryContext<?> ctx : pending) {
                FrameHelper.sendFrame("send", remoteNodeId, ctx.message, socket);
            }
        }
        pending = null;
    }
}
