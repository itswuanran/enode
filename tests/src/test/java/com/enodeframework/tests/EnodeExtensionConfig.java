package com.enodeframework.tests;

import com.enodeframework.ENodeBootstrap;
import com.enodeframework.commanding.impl.DefaultCommandProcessor;
import com.enodeframework.commanding.impl.DefaultProcessingCommandHandler;
import com.enodeframework.eventing.impl.DefaultEventService;
import com.enodeframework.eventing.impl.InMemoryEventStore;
import com.enodeframework.infrastructure.impl.InMemoryPublishedVersionStore;
import com.enodeframework.mysql.MysqlEventStore;
import com.enodeframework.mysql.MysqlPublishedVersionStore;
import com.enodeframework.queue.command.CommandResultProcessor;
import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(value = "com.enodeframework")
public class EnodeExtensionConfig {
    public static String JDBC_URL = "jdbc:mysql://p.anruence.com:13306/enode";

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public CommandResultProcessor commandResultProcessor() {
        CommandResultProcessor processor = new CommandResultProcessor();
        return processor;
    }

    @Bean(initMethod = "init")
    public ENodeBootstrap eNodeBootstrap() {
        ENodeBootstrap bootstrap = new ENodeBootstrap();
        bootstrap.setPackages(Lists.newArrayList("com.enodeframework.tests"));
        return bootstrap;
    }

    @Bean
    public DefaultProcessingCommandHandler defaultProcessingCommandHandler() {
        return new DefaultProcessingCommandHandler();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public DefaultEventService defaultEventService() {
        return new DefaultEventService();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public DefaultCommandProcessor defaultCommandProcessor() {
        return new DefaultCommandProcessor();
    }

    @Bean
    public InMemoryEventStore eventStore() {
        return new InMemoryEventStore();
    }

    @Bean
    public InMemoryPublishedVersionStore publishedVersionStore() {
        return new InMemoryPublishedVersionStore();
    }
//    @Bean
//    public MysqlEventStore mysqlEventStore(HikariDataSource dataSource) {
//        MysqlEventStore mysqlEventStore = new MysqlEventStore(dataSource, null);
//        return mysqlEventStore;
//    }
//
//    @Bean
//    public HikariDataSource dataSource() {
//        HikariDataSource dataSource = new HikariDataSource();
//        dataSource.setJdbcUrl(JDBC_URL);
//        dataSource.setUsername("root");
//        dataSource.setPassword("root");
//        dataSource.setDriverClassName(com.mysql.cj.jdbc.Driver.class.getName());
//        return dataSource;
//    }
//
//    @Bean
//    public MysqlPublishedVersionStore mysqlPublishedVersionStore(HikariDataSource dataSource) {
//        return new MysqlPublishedVersionStore(dataSource, null);
//    }
}