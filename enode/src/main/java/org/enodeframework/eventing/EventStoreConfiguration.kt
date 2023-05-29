package org.enodeframework.eventing


interface EventStoreConfiguration {
    /**
     * database名称
     */
    var dbName: String

    /**
     * 事件表的默认名称；默认为：event_stream
     */
    var eventTableName: String

    /**
     * 聚合根已发布事件表的默认名称；默认为：published_version
     */
    var publishedTableName: String

    /**
     * 事件表的聚合根版本唯一索引的默认名称；默认为：uk_aggregate_root_id_version
     */
    var eventVersionUkName: String

    /**
     * 事件表的聚合根已处理命令唯一索引的默认名称；默认为：uk_aggregate_root_id_command_id
     */
    var eventCommandIdUkName: String

    /**
     * 聚合根已发布事件表的聚合根已发布版本唯一索引的默认名称；默认为：uk_processor_name_aggregate_root_id_version
     */
    var publishedUkName: String

    /**
     * 通过异常消息解析CommandId
     */
    fun seekCommandId(msg: String): String
}