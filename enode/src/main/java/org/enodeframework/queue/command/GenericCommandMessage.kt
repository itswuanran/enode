package org.enodeframework.queue.command

import java.io.Serializable

/**
 * @author anruence@gmail.com
 */
class GenericCommandMessage : Serializable {
    var commandType: String = ""
    var commandData: String = ""
    var replyAddress: String = ""
}
