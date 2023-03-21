package org.enodeframework.queue;

import com.google.common.collect.Lists;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.impl.Arguments;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.core.spi.cluster.NodeSelector;
import io.vertx.core.spi.cluster.RegistrationUpdateEvent;

/**
 * @author anruence@gmail.com
 */
public class SingleNodeSelector implements NodeSelector {
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
