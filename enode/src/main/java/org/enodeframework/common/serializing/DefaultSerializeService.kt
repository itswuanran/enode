package org.enodeframework.common.serializing

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import org.enodeframework.common.exception.EnodeException

class DefaultSerializeService : SerializeService {
    override fun <T> deserialize(value: String, type: Class<T>): T {
        return try {
            MAPPER.readValue(value, type)
        } catch (e: JsonProcessingException) {
            throw EnodeException(e)
        }
    }

    override fun <T> deserializeBytes(value: ByteArray, type: Class<T>): T {
        return try {
            MAPPER.readValue(value, type)
        } catch (e: JsonProcessingException) {
            throw EnodeException(e)
        }
    }

    override fun serialize(target: Any): String {
        return try {
            MAPPER.writeValueAsString(target)
        } catch (e: JsonProcessingException) {
            throw EnodeException(e)
        }
    }

    override fun serializeBytes(target: Any): ByteArray {
        return try {
            MAPPER.writeValueAsBytes(target)
        } catch (e: JsonProcessingException) {
            throw EnodeException(e)
        }
    }

    companion object {
        private val MAPPER = JsonMapper.builder()
            //序列化时，遇到空bean（无属性）时不会失败
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            //反序列化时，遇到未知属性（在bean上找不到对应属性）时不会失败
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            //通过fields来探测（不通过标准getter探测）
            .configure(MapperFeature.AUTO_DETECT_FIELDS, true)
            .build()

        init {
            //序列化时，跳过null属性
            MAPPER.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
        }
    }
}