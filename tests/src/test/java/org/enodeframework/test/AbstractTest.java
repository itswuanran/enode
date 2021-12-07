package org.enodeframework.test;

import org.enodeframework.commanding.CommandBus;
import org.enodeframework.domain.DomainExceptionMessage;
import org.enodeframework.domain.MemoryCache;
import org.enodeframework.eventing.DomainEventStream;
import org.enodeframework.eventing.EventStore;
import org.enodeframework.eventing.ProcessingEventProcessor;
import org.enodeframework.eventing.PublishedVersionStore;
import org.enodeframework.messaging.ApplicationMessage;
import org.enodeframework.messaging.MessagePublisher;
import org.enodeframework.test.config.EnodeTestDataSourceConfig;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {EnodeTestDataSourceConfig.class})
@SpringBootTest(classes = App.class)
public abstract class AbstractTest {
    @Autowired
    protected CommandBus commandService;
    @Autowired
    protected MemoryCache memoryCache;
    @Autowired
    protected EventStore eventStore;
    @Autowired
    protected PublishedVersionStore publishedVersionStore;
    @Autowired
    @Qualifier(value = "defaultDomainEventPublisher")
    protected MessagePublisher<DomainEventStream> domainEventPublisher;
    @Autowired
    @Qualifier(value = "defaultApplicationMessagePublisher")
    protected MessagePublisher<ApplicationMessage> applicationMessagePublisher;
    @Autowired
    @Qualifier(value = "defaultPublishableExceptionPublisher")
    protected MessagePublisher<DomainExceptionMessage> publishableExceptionPublisher;
    @Autowired
    protected ProcessingEventProcessor processor;
}
