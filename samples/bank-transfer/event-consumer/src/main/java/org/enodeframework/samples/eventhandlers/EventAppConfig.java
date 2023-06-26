package org.enodeframework.samples.eventhandlers;

import com.zaxxer.hikari.HikariDataSource;
import io.vertx.core.Vertx;
import io.vertx.jdbcclient.JDBCPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventAppConfig {

    @Value("${spring.enode.datasource.jdbcurl:}")
    private String jdbcUrl;

    @Value("${spring.enode.datasource.username:}")
    private String username;

    @Value("${spring.enode.datasource.password:}")
    private String password;

    @Bean("enodeJDBCPool")
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "jdbc-mysql")
    public JDBCPool enodeJDBCPool(Vertx vertx) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(com.mysql.cj.jdbc.Driver.class.getName());
        return JDBCPool.pool(vertx, dataSource);
    }

    @Bean
    public Vertx vertx() {
        Vertx vertx = Vertx.vertx();
        return vertx;
    }

}
