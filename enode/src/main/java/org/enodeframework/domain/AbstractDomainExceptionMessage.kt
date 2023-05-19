package org.enodeframework.domain

import com.google.common.collect.Maps
import org.enodeframework.common.exception.EnodeException
import org.enodeframework.common.utils.IdGenerator
import java.util.*

abstract class AbstractDomainExceptionMessage @JvmOverloads constructor(override var id: String = IdGenerator.id()) :
    EnodeException(), DomainExceptionMessage {
    override var timestamp: Date = Date()
    override var items: MutableMap<String, Any> = Maps.newHashMap()

    abstract override fun serializeTo(serializableInfo: MutableMap<String, Any>)
    abstract override fun restoreFrom(serializableInfo: MutableMap<String, Any>)
}
