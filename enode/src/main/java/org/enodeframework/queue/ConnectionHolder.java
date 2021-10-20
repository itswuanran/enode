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

import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.impl.ConnectionBase;
import io.vertx.ext.eventbus.bridge.tcp.impl.protocol.FrameHelper;
import org.enodeframework.common.exception.ReplyAddressException;
import org.enodeframework.common.utils.ReplyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.Queue;

public class ConnectionHolder {

    private static final Logger log = LoggerFactory.getLogger(ConnectionHolder.class);

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

    void connect() {
        URI info = ReplyUtil.toURI(remoteNodeAddress).orElseThrow(
            () -> new ReplyAddressException(String.format("Parse remoteNodeAddress: %s  failed, please check.", remoteNodeAddress)));
        eventBus.client().connect(info.getPort(), info.getHost())
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
