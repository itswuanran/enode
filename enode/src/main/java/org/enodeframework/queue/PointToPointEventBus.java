/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package org.enodeframework.queue;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.eventbus.impl.EventBusImpl;
import io.vertx.core.eventbus.impl.MessageImpl;
import io.vertx.core.eventbus.impl.OutboundDeliveryContext;
import io.vertx.core.eventbus.impl.clustered.Serializer;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.spi.cluster.NodeSelector;

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
        nodeId = "";
        nodeSelector = new SingleNodeSelector();
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

