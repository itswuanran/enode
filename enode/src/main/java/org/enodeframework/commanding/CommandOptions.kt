package org.enodeframework.commanding


interface CommandOptions {
    var host: String
    var port: Int
    var replyTopic: String
    var timeoutMs: Int

    fun address(): String {
        return "enode://$host:$port"
    }

    /**
     * default reply to topic#host:port
     */
    fun replyTo(): String

    /**
     * reply to topic#tag
     */
    fun replyWith(tag: String): String

}