package org.enodeframework.spring

import org.enodeframework.eventing.EventStoreConfiguration
import java.util.regex.Pattern

/**
 * 可以将EventStore的元数据全部配置化
 *
 * @author anruence@gmail.com
 */
class DefaultEventStoreConfiguration : EventStoreConfiguration {
    /**
     * E11000 duplicate key error collection: enode.event_stream index: aggregateRootId_1_commandId_1 dup key: { aggregateRootId: "5ee8b610d7671114741829c7", commandId: "5ee8b61bd7671114741829cf" }
     */
    private val seekIdPattern: Pattern =
        Pattern.compile("^Duplicate entry '.*-(.*)' for key|=\\(.*, (.*)\\) already exists.|\\{.+?commandId: \"(.+?)\" }\$")

    override var dbName: String = "enode"

    /**
     * 事件表的默认名称；默认为：event_stream
     */
    override var eventTableName: String = "event_stream"

    /**
     * 聚合根已发布事件表的默认名称；默认为：published_version
     */
    override var publishedTableName: String = "published_version"

    /**
     * 事件表的聚合根版本唯一索引的默认名称；默认为：uk_aggregate_root_id_version
     */
    override var eventVersionUkName: String = "uk_aggregate_root_id_version"

    /**
     * 事件表的聚合根已处理命令唯一索引的默认名称；默认为：uk_aggregate_root_id_command_id
     */
    override var eventCommandIdUkName: String = "uk_aggregate_root_id_command_id"

    /**
     * 聚合根已发布事件表的聚合根已发布版本唯一索引的默认名称；默认为：uk_processor_name_aggregate_root_id_version
     */
    override var publishedUkName: String = "uk_processor_name_aggregate_root_id_version"

    override fun seekCommandId(msg: String): String {
        val matcher = seekIdPattern.matcher(msg)
        if (!matcher.find()) {
            return ""
        }
        return if (matcher.groupCount() == 0) {
            ""
        } else matcher.group(1)
    }

    companion object Driver {

        fun mysql(): DefaultEventStoreConfiguration {
            val option = DefaultEventStoreConfiguration()
            option.dbName = "enode"
            option.eventTableName = "event_stream"
            option.publishedTableName = "published_version"
            option.eventVersionUkName = "uk_aggregate_root_id_version"
            option.eventCommandIdUkName = "uk_aggregate_root_id_command_id"
            option.publishedUkName = "uk_processor_name_aggregate_root_id"
            return option
        }

        fun mongo(): DefaultEventStoreConfiguration {
            val option = DefaultEventStoreConfiguration()
            option.dbName = "enode"
            option.eventTableName = "event_stream"
            option.publishedTableName = "published_version"
            option.eventVersionUkName = "aggregateRootId_1_version_1"
            option.eventCommandIdUkName = "aggregateRootId_1_commandId_1"
            option.publishedUkName = "processorName_1_aggregateRootId_1"
            return option
        }

        fun pg(): DefaultEventStoreConfiguration {
            val option = DefaultEventStoreConfiguration()
            option.dbName = "enode"
            option.eventTableName = "event_stream"
            option.publishedTableName = "published_version"
            option.eventVersionUkName = "uk_aggregate_root_id_version"
            option.eventCommandIdUkName = "uk_aggregate_root_id_command_id"
            option.publishedUkName = "uk_processor_name_aggregate_root_id"
            return option
        }
    }
}