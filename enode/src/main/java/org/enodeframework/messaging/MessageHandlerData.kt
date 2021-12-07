package org.enodeframework.messaging

import org.enodeframework.infrastructure.ObjectProxy

/**
 * @author anruence@gmail.com
 */
class MessageHandlerData<T : ObjectProxy> {
    var allHandlers: List<T> = ArrayList()
    var listHandlers: List<T> = ArrayList()
    var queuedHandlers: List<T> = ArrayList()
}