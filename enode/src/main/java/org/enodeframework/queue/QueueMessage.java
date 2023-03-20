package org.enodeframework.queue;

import com.google.common.base.MoreObjects;

import java.io.Serializable;

/**
 * @author anruence@gmail.com
 */
public class QueueMessage implements Serializable {
    /**
     * 消息体
     */
    private String body;
    /**
     * topic
     */
    private String topic;
    /**
     * 业务标识
     */
    private String tag;
    /**
     * 路由的键
     */
    private String routeKey;
    /**
     * 消息唯一标识
     */
    private String key;
    /**
     * 消息类型
     * {@link MessageTypeCode}
     */
    private Character type;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getRouteKey() {
        return routeKey;
    }

    public void setRouteKey(String routeKey) {
        this.routeKey = routeKey;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Character getType() {
        return type;
    }

    public void setType(Character type) {
        this.type = type;
    }

    public String getBodyAndType() {
        return body + "|" + type;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("body", body)
            .add("topic", topic)
            .add("tag", tag)
            .add("routeKey", routeKey)
            .add("key", key)
            .add("type", type)
            .toString();
    }
}
