package org.enodeframework.samples.controller;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import org.enodeframework.queue.DefaultSendReplyService;
import org.enodeframework.queue.command.DefaultCommandResultProcessor;
import org.enodeframework.spring.EnableEnode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication(scanBasePackages = {"org.enodeframework"})
@EnableEnode(scanBasePackages = {"org.enodeframework"})
@EnableWebFlux
public class WebApplication {
    @Autowired
    private DefaultCommandResultProcessor commandResultProcessor;
    @Autowired
    private DefaultSendReplyService sendReplyService;

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }

    @Bean
    public Vertx vertx() {
        VertxOptions options = new VertxOptions();
        Vertx vertx = Vertx.vertx();
        vertx.isClustered();
        vertx.deployVerticle(commandResultProcessor);
        vertx.deployVerticle(sendReplyService);
        return vertx;
    }
}