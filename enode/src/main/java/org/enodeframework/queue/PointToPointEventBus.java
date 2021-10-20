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

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetServerOptions;

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

    private NetServerOptions getServerOptions() {
        return new NetServerOptions(this.options.toJson());
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

