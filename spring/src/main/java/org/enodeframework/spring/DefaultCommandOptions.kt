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
        return replyWith(address())
    }

    override fun replyWith(tag: String): String {
        val value = encode(tag)
        return "$replyTopic-$value"
    }

    private fun encode(value: String): String {
        return value.replace(".", "-").replace("://","h").replace(":","p")
    }
}