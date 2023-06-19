package org.enodeframework.spring

import com.google.common.base.Strings
import org.enodeframework.eventing.EventStoreOptions
import java.util.regex.Pattern

/**
 * 可以将EventStore的元数据全部配置化
 *
 * @author anruence@gmail.com
 */
class DefaultEventStoreOptions : EventStoreOptions {
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
     * 聚合根已发布事件表的聚合根已发布版本唯一索引的默认名称；默认为：uk_aggregate_root_id_version_processor_name
     */
    override var publishedUkName: String = "uk_aggregate_root_id_version_processor_name"

    override fun seekCommandId(msg: String): String {
        val matcher = seekIdPattern.matcher(msg)
        if (!matcher.find()) {
            return ""
        }
        for (i in 1..matcher.groupCount()) {
            if (Strings.isNullOrEmpty(matcher.group(i))) {
                continue
            }
            return matcher.group(i)
        }
        return ""
    }

    companion object Driver {

        fun mysql(): DefaultEventStoreOptions {
            val option = DefaultEventStoreOptions()
            option.dbName = "enode"
            option.eventTableName = "event_stream"
            option.publishedTableName = "published_version"
            option.eventVersionUkName = "uk_aggregate_root_id_version"
            option.eventCommandIdUkName = "uk_aggregate_root_id_command_id"
            option.publishedUkName = "uk_aggregate_root_id_version_processor_name"
            return option
        }

        fun mongo(): DefaultEventStoreOptions {
            val option = DefaultEventStoreOptions()
            option.dbName = "enode"
            option.eventTableName = "event_stream"
            option.publishedTableName = "published_version"
            option.eventVersionUkName = "aggregateRootId_1_version_1"
            option.eventCommandIdUkName = "aggregateRootId_1_commandId_1"
            option.publishedUkName = "aggregateRootId_1_version_1_processorName_1"
            return option
        }

        fun pg(): DefaultEventStoreOptions {
            val option = DefaultEventStoreOptions()
            option.dbName = "enode"
            option.eventTableName = "event_stream"
            option.publishedTableName = "published_version"
            option.eventVersionUkName = "uk_aggregate_root_id_version"
            option.eventCommandIdUkName = "uk_aggregate_root_id_command_id"
            option.publishedUkName = "uk_aggregate_root_id_version_processor_name"
            return option
        }
    }
}