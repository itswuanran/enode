package org.enodeframework.commanding;

import com.google.common.collect.Maps;
import org.enodeframework.common.utils.Assert;
import org.enodeframework.messaging.AbstractMessage;

import java.util.Map;
import java.util.Objects;

/**
 * @author anruence@gmail.com
 */
public abstract class AbstractCommandMessage<TAggregateRootId> extends AbstractMessage implements CommandMessage<TAggregateRootId> {

    public TAggregateRootId aggregateRootId;

    public AbstractCommandMessage() {
        super();
    }

    public AbstractCommandMessage(TAggregateRootId aggregateRootId) {
        this(aggregateRootId, Maps.newHashMap());
    }

    public AbstractCommandMessage(TAggregateRootId aggregateRootId, Map<String, Object> items) {
        super();
        Assert.nonNull(aggregateRootId, "aggregateRootId");
        this.aggregateRootId = aggregateRootId;
        this.items = items;
    }

    /**
     * Init command with id and aggregateRootId
     */
    public AbstractCommandMessage(String id, TAggregateRootId aggregateRootId) {
        this(id, aggregateRootId, Maps.newHashMap());
    }

    public AbstractCommandMessage(String id, TAggregateRootId aggregateRootId, Map<String, Object> items) {
        super(id);
        Assert.nonNull(aggregateRootId, "aggregateRootId");
        this.aggregateRootId = aggregateRootId;
        this.items = items;
    }

    @Override
    public TAggregateRootId getAggregateRootId() {
        return aggregateRootId;
    }

    @Override
    public String getAggregateRootIdAsString() {
        return Objects.toString(aggregateRootId, "");
    }

    public void setAggregateRootId(TAggregateRootId aggregateRootId) {
        this.aggregateRootId = aggregateRootId;
    }
}
