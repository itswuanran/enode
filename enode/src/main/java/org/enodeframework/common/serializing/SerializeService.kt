package org.enodeframework.common.serializing

interface SerializeService {
    /**
     * deserialize the value to given type
     */
    fun <T> deserialize(value: String, type: Class<T>): T

    /**
     * serialize the value to string
     */
    fun serialize(target: Any): String
}