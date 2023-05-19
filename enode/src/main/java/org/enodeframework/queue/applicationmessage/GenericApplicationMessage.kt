package org.enodeframework.queue.applicationmessage

import java.io.Serializable

/**
 * @author anruence@gmail.com
 */
class GenericApplicationMessage : Serializable {
    var applicationMessageData: String = ""
    var applicationMessageType: String = ""
}
