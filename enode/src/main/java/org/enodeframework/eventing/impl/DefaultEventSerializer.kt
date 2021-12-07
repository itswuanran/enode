package org.enodeframework.eventing.impl

import com.google.common.collect.Maps
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
    override fun serialize(evnts: List<DomainEventMessage<*>>): Map<String, String> {
        val dict = Maps.newLinkedHashMap<String, String>()
        evnts.forEach { evnt: DomainEventMessage<*> ->
            val typeName = typeNameProvider.getTypeName(evnt.javaClass)
            val eventData = serializeService.serialize(evnt)
            dict[typeName] = eventData
        }
        return dict
    }

    override fun deserialize(data: Map<String, String>): List<DomainEventMessage<*>> {
        val evnts: MutableList<DomainEventMessage<*>> = ArrayList()
        data.forEach { (key: String, value: String) ->
            val eventType = typeNameProvider.getType(key)
            val evnt = serializeService.deserialize(value, eventType) as DomainEventMessage<*>
            evnts.add(evnt)
        }
        return evnts
    }
}