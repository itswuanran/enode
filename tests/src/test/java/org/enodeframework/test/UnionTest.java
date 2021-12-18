package org.enodeframework.test;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(initializers = UnionTest.KafkaPgInitializer.class)
public class UnionTest extends EnodeCoreTest {

    static class RocketMQMySQLInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of("spring.enode.eventstore=mysql").applyTo(applicationContext);
            TestPropertyValues.of("spring.enode.mq=rocketmq").applyTo(applicationContext);
        }
    }

    static class RocketMQpgInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of("spring.enode.eventstore=pg").applyTo(applicationContext);
            TestPropertyValues.of("spring.enode.mq=rocketmq").applyTo(applicationContext);
        }
    }

    static class RocketMQMongoInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of("spring.enode.eventstore=mongo").applyTo(applicationContext);
            TestPropertyValues.of("spring.enode.mq=rocketmq").applyTo(applicationContext);
        }
    }

    static class RocketMQJDBCMySQLInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of("spring.enode.eventstore=jdbc-mysql").applyTo(applicationContext);
            TestPropertyValues.of("spring.enode.mq=rocketmq").applyTo(applicationContext);
        }
    }

    static class RocketMQJDBCPgInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of("spring.enode.eventstore=jdbc-pg").applyTo(applicationContext);
            TestPropertyValues.of("spring.enode.mq=rocketmq").applyTo(applicationContext);
        }
    }

    // kafka
    static class KafkaMySQLInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of("spring.enode.eventstore=mysql").applyTo(applicationContext);
            TestPropertyValues.of("spring.enode.mq=kafka").applyTo(applicationContext);
        }
    }

    static class KafkaPgInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of("spring.enode.eventstore=pg").applyTo(applicationContext);
            TestPropertyValues.of("spring.enode.mq=kafka").applyTo(applicationContext);
        }
    }

    static class KafkaMongoInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of("spring.enode.eventstore=mongo").applyTo(applicationContext);
            TestPropertyValues.of("spring.enode.mq=kafka").applyTo(applicationContext);
        }
    }

    static class KafkaJDBCMySQLInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of("spring.enode.eventstore=jdbc-mysql").applyTo(applicationContext);
            TestPropertyValues.of("spring.enode.mq=kafka").applyTo(applicationContext);
        }
    }

    static class KafkaJDBCPgInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of("spring.enode.eventstore=jdbc-pg").applyTo(applicationContext);
            TestPropertyValues.of("spring.enode.mq=kafka").applyTo(applicationContext);
        }
    }

    // pulsar
    static class PulsarMySQLInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of("spring.enode.eventstore=mysql").applyTo(applicationContext);
            TestPropertyValues.of("spring.enode.mq=pulsar").applyTo(applicationContext);
        }
    }

    static class PulsarpgInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of("spring.enode.eventstore=pg").applyTo(applicationContext);
            TestPropertyValues.of("spring.enode.mq=pulsar").applyTo(applicationContext);
        }
    }

    static class PulsarMongoInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of("spring.enode.eventstore=mongo").applyTo(applicationContext);
            TestPropertyValues.of("spring.enode.mq=pulsar").applyTo(applicationContext);
        }
    }

    static class PulsarJDBCMySQLInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of("spring.enode.eventstore=jdbc-mysql").applyTo(applicationContext);
            TestPropertyValues.of("spring.enode.mq=pulsar").applyTo(applicationContext);
        }
    }

    static class PulsarJDBCPgInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of("spring.enode.eventstore=jdbc-pg").applyTo(applicationContext);
            TestPropertyValues.of("spring.enode.mq=pulsar").applyTo(applicationContext);
        }
    }

}
