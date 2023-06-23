package org.enodeframework.spring

import org.enodeframework.commanding.CommandOptions

/**
 * @author anruence@gmail.com
 */
class DefaultCommandOptions(
    override var host: String,
    override var port: Int,
    override var timeoutMs: Int = 10000,
    override var replyTopic: String,
) : CommandOptions {
    override fun replyTo(): String {
        val xx = address()
        return "$replyTopic#$xx"
    }

    override fun replyWith(tag: String): String {
        return "$replyTopic#$tag"
    }
}