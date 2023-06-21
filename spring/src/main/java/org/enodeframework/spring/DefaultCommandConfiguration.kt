package org.enodeframework.spring

import org.enodeframework.commanding.CommandConfiguration

/**
 * @author anruence@gmail.com
 */
class DefaultCommandConfiguration(
    override var host: String,
    override var port: Int,
    override var timeoutMs: Int = 10000,
    override var replyTopic: String
) : CommandConfiguration {
    override fun replyTo(): String {
        return "$replyTopic#enode://${this.host}:${this.port}"
    }

    override fun replyTo(tag: String): String {
        return "$replyTopic#$tag"
    }
}