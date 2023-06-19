package org.enodeframework.common.serializing

interface SerializeService {
    /**
     * deserialize the value to given type
     */
    fun <T> deserialize(value: String, type: Class<T>): T
    fun <T> deserializeBytes(value: ByteArray, type: Class<T>): T

    /**
     * serialize the value to string
     */
    fun serialize(target: Any): String
    fun serializeBytes(target: Any): ByteArray
}