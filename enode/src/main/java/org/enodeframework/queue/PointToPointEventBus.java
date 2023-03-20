package org.enodeframework.queue;

import com.google.common.collect.Lists;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.impl.EventBusImpl;
import io.vertx.core.eventbus.impl.MessageImpl;
import io.vertx.core.eventbus.impl.OutboundDeliveryContext;
import io.vertx.core.eventbus.impl.clustered.Serializer;
import io.vertx.core.impl.Arguments;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.ConnectionBase;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.core.spi.cluster.NodeSelector;
import io.vertx.core.spi.cluster.RegistrationUpdateEvent;
import io.vertx.ext.eventbus.bridge.tcp.impl.protocol.FrameHelper;
import org.enodeframework.common.exception.ReplyAddressException;
import org.enodeframework.common.utils.ReplyUtil;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * An event bus implementation that point to point with other Vert.x nodes
 */
public class PointToPointEventBus extends EventBusImpl {
    private static final Logger log = LoggerFactory.getLogger(PointToPointEventBus.class);
    private final EventBusOptions options;
    private final NetClient client;
    private final Vertx vertx;
    private final ConcurrentMap<String, ConnectionHolder> connections = new ConcurrentHashMap<>();
    private String nodeId;
    private NodeSelector nodeSelector;

    public PointToPointEventBus(Vertx vertx, VertxOptions options) {
        super((VertxInternal) vertx);
        this.vertx = vertx;
        this.options = options.getEventBusOptions();
        this.client = vertx.createNetClient(new NetClientOptions(this.options.toJson()));
    }

    ConcurrentMap<String, ConnectionHolder> connections() {
        return connections;
    }

    Vertx vertx() {
        return vertx;
    }

    EventBusOptions options() {
        return options;
    }

    NetClient client() {
        return client;
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

    @Override
    public void start(Promise<Void> promise) {
        if (started) {
            throw new IllegalStateException("Already started");
        }
        nodeId = "";
        nodeSelector = new SingleNodeSelector();
        started = true;
        promise.complete();
    }

    @Override
    protected <T> void sendOrPub(io.vertx.core.eventbus.impl.OutboundDeliveryContext<T> sendContext) {
        Serializer serializer = Serializer.get(sendContext.ctx);
        if (sendContext.message.isSend()) {
            Promise<String> promise = sendContext.ctx.promise();
            serializer.queue(sendContext.message, nodeSelector::selectForSend, promise);
            promise.future().onComplete(ar -> {
                if (ar.succeeded()) {
                    sendToNode(sendContext, ar.result());
                } else {
                    sendOrPublishFailed(sendContext, ar.cause());
                }
            });
        } else {
            Promise<Iterable<String>> promise = sendContext.ctx.promise();
            serializer.queue(sendContext.message, nodeSelector::selectForPublish, promise);
            promise.future().onComplete(ar -> {
                if (ar.succeeded()) {
                    sendToNodes(sendContext, ar.result());
                } else {
                    sendOrPublishFailed(sendContext, ar.cause());
                }
            });
        }
    }

    private void sendOrPublishFailed(io.vertx.core.eventbus.impl.OutboundDeliveryContext<?> sendContext, Throwable cause) {
        if (log.isDebugEnabled()) {
            log.error("Failed to send message", cause);
        }
        sendContext.written(cause);
    }

    @Override
    protected boolean isMessageLocal(MessageImpl msg) {
        return false;
    }

    private <T> void sendToNode(OutboundDeliveryContext<T> sendContext, String nodeId) {
        if (nodeId != null && !nodeId.equals(this.nodeId)) {
            sendRemote(sendContext, nodeId);
        } else {
            super.sendOrPub(sendContext);
        }
    }

    private <T> void sendToNodes(OutboundDeliveryContext<T> sendContext, Iterable<String> nodeIds) {
        boolean sentRemote = false;
        if (nodeIds != null) {
            for (String nid : nodeIds) {
                if (!sentRemote) {
                    sentRemote = true;
                }
                sendToNode(sendContext, nid);
            }
        }
        if (!sentRemote) {
            super.sendOrPub(sendContext);
        }
    }

    private void sendRemote(OutboundDeliveryContext<?> sendContext, String remoteNodeId) {
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

}

class SingleNodeSelector implements NodeSelector {
    @Override
    public void init(Vertx vertx, ClusterManager clusterManager) {
    }

    @Override
    public void eventBusStarted() {
    }

    @Override
    public void selectForSend(Message<?> message, Promise<String> promise) {
        Arguments.require(message.isSend(), "selectForSend used for publishing");
        promise.tryComplete(message.address());
    }

    @Override
    public void selectForPublish(Message<?> message, Promise<Iterable<String>> promise) {
        Arguments.require(!message.isSend(), "selectForPublish used for sending");
        promise.tryComplete(Lists.newArrayList(message.address()));
    }

    @Override
    public void registrationsUpdated(RegistrationUpdateEvent event) {
    }

    @Override
    public void registrationsLost() {
    }
}

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
        SocketAddress socketAddress = ReplyUtil.toSocketAddress(remoteNodeId);
        if (socketAddress == null) {
            throw new ReplyAddressException(String.format("Parse remoteNodeId [%s] failed", remoteNodeId));
        }
        eventBus.client().connect(socketAddress.port(), socketAddress.host()).onComplete(ar -> {
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
        socket.exceptionHandler(this::close);
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