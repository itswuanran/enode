package org.enodeframework.samples.controller;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.enodeframework.spring.EnableEnode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication(scanBasePackages = {"org.enodeframework"})
@EnableEnode(scanBasePackages = {"org.enodeframework"})
@EnableWebFlux
public class WebApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }

    @Bean
    public Vertx vertx() {
        VertxOptions options = new VertxOptions();
        Vertx vertx = Vertx.vertx();
        return vertx;
    }
}