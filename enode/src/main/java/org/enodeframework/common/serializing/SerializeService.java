package org.enodeframework.common.serializing;

public interface SerializeService {

    <T> T deserialize(String json, Class<T> type);

    String serialize(Object target);
}