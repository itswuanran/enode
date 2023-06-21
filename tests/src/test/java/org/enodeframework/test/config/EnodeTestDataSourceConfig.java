package org.enodeframework.test.config;

import com.zaxxer.hikari.HikariDataSource;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import org.enodeframework.jdbc.JDBCEventStore;
import org.enodeframework.jdbc.JDBCPublishedVersionStore;
import org.enodeframework.vertx.message.TcpSendReplyService;
import org.enodeframework.vertx.message.TcpServerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

public class EnodeTestDataSourceConfig {

    @Value("${spring.enode.datasource.jdbcurl:}")
    private String jdbcUrl;

    @Value("${spring.enode.datasource.username:}")
    private String username;

    @Value("${spring.enode.datasource.password:}")
    private String password;

    @Value("${spring.enode.datasource.pg.jdbcurl:}")
    private String pgJdbcUrl;

    @Value("${spring.enode.datasource.pg.username:}")
    private String pgUsername;

    @Value("${spring.enode.datasource.pg.password:}")
    private String pgPassword;

    @Autowired(required = false)
    private JDBCEventStore jdbcEventStore;

    @Autowired(required = false)
    private JDBCPublishedVersionStore jdbcPublishedVersionStore;

    @Autowired(required = false)
    private TcpSendReplyService tcpSendReplyService;

    @Autowired(required = false)
    private TcpServerListener tcpServerListener;

    @Bean
    public Vertx vertx() {
        Vertx vertx = Vertx.vertx();
        if (tcpSendReplyService != null) {
            vertx.deployVerticle(tcpSendReplyService);
        }
        if (tcpServerListener != null) {
            vertx.deployVerticle(tcpServerListener);
        }
        if (jdbcEventStore != null) {
            vertx.deployVerticle(jdbcEventStore);
        }
        if (jdbcPublishedVersionStore != null) {
            vertx.deployVerticle(jdbcPublishedVersionStore);
        }
        return vertx;
    }

    @Bean("enodeMongoClient")
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "mongo")
    public MongoClient mongoClient(Vertx vertx) {
        return MongoClient.create(vertx, new JsonObject().put("db_name", "test"));
    }

    @Bean("enodeMySQLPool")
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "mysql")
    public MySQLPool enodeMySQLPool() {
        MySQLConnectOptions connectOptions = MySQLConnectOptions.fromUri(jdbcUrl.replaceAll("jdbc:", ""))
            .setUser(username)
            .setPassword(password);
        PoolOptions poolOptions = new PoolOptions()
            .setMaxSize(5);
        return MySQLPool.pool(connectOptions, poolOptions);
    }

    @Bean("enodePgPool")
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "pg")
    public PgPool pgPool() {
        PgConnectOptions connectOptions = PgConnectOptions.fromUri(pgJdbcUrl.replaceAll("jdbc:", ""))
            .setUser(pgUsername)
            .setPassword(pgPassword);
        PoolOptions poolOptions = new PoolOptions()
            .setMaxSize(5);
        return PgPool.pool(connectOptions, poolOptions);
    }

    @Bean("enodeMySQLDataSource")
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "jdbc-mysql")
    public DataSource enodeMySQLDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(com.mysql.cj.jdbc.Driver.class.getName());
        return dataSource;
    }

    @Bean("enodePgDataSource")
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "jdbc-pg")
    public DataSource enodePgDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(pgJdbcUrl);
        dataSource.setUsername(pgUsername);
        dataSource.setPassword(pgPassword);
        dataSource.setDriverClassName(org.postgresql.Driver.class.getName());
        return dataSource;
    }
}
