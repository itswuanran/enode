package org.enodeframework.eventing.impl

import org.enodeframework.common.serializing.SerializeService
import org.enodeframework.eventing.DomainEventMessage
import org.enodeframework.eventing.EventSerializer
import org.enodeframework.infrastructure.TypeNameProvider

/**
 * @author anruence@gmail.com
 */
class DefaultEventSerializer(
    private val typeNameProvider: TypeNameProvider,
    private val serializeService: SerializeService
) : EventSerializer {
    override fun serialize(evnts: List<DomainEventMessage>): Map<String, String> {
        return evnts.associateBy({ k -> typeNameProvider.getTypeName(k.javaClass) },
            { v -> serializeService.serialize(v) })
    }

    override fun deserialize(data: Map<String, String>): List<DomainEventMessage> {
        return data.map { (key, value) ->
            val eventType = typeNameProvider.getType(key)
            serializeService.deserialize(value, eventType) as DomainEventMessage
        }
    }
}