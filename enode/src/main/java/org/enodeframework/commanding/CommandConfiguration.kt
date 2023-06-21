package org.enodeframework.commanding


interface CommandConfiguration {
    var host: String
    var port: Int
    var replyTopic: String
    var timeoutMs: Int
    fun replyTo(): String
    fun replyTo(tag: String): String

}