package org.enodeframework.messaging

import org.enodeframework.common.utils.IdGenerator

abstract class AbstractMessage @JvmOverloads constructor(override var id: String = IdGenerator.id()) : Message
