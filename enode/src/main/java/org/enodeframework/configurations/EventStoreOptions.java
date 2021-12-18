package org.enodeframework.configurations;

import io.vertx.core.json.JsonObject;

import java.util.regex.Pattern;

/**
 * 可以将EventStore的元数据全部配置化
 *
 * @author anruence@gmail.com
 */
public class EventStoreOptions {

    public static final Pattern MYSQL_PATTERN = Pattern.compile("^Duplicate entry '.*-(.*)' for key");

    public static final Pattern PG_PATTERN = Pattern.compile("=\\(.*, (.*)\\) already exists.");

    public static final Pattern MONGO_PATTERN = Pattern.compile("\\{.+?commandId: \"(.+?)\" }$");

    /**
     * MongoDB 为应用创建的database名称
     */
    private String dbName;
    /**
     * 事件表的默认名称；默认为：event_stream
     */
    private String eventTableName;
    /**
     * 聚合根已发布事件表的默认名称；默认为：published_version
     */
    private String publishedTableName;
    /**
     * 事件表的聚合根版本唯一索引的默认名称；默认为：uk_aggregate_root_id_version
     */
    private String eventVersionUkName;
    /**
     * 事件表的聚合根已处理命令唯一索引的默认名称；默认为：uk_aggregate_root_id_command_id
     */
    private String eventCommandIdUkName;
    /**
     * 聚合根已发布事件表的聚合根已发布版本唯一索引的默认名称；默认为：uk_processor_name_aggregate_root_id_version
     */
    private String publishedUkName;
    /**
     * 解析CommandId的正则表达式
     */
    private Pattern commandIdPattern;
    /**
     * 事件表的元数据
     */
    private JsonObject eventMeta;
    /**
     * 发布版本表的元数据
     */
    private JsonObject publishedVersionMeta;

    public static EventStoreOptions pgMysql() {
        return pg();
    }

    public static EventStoreOptions jdbcMysql() {
        return mysql();
    }

    public static EventStoreOptions mysql() {
        EventStoreOptions option = new EventStoreOptions();
        option.setDbName("enode");
        option.setCommandIdPattern(MYSQL_PATTERN);
        option.setEventTableName("event_stream");
        option.setPublishedTableName("published_version");
        option.setEventVersionUkName("uk_aggregate_root_id_version");
        option.setEventCommandIdUkName("uk_aggregate_root_id_command_id");
        option.setPublishedUkName("uk_processor_name_aggregate_root_id");
        return option;
    }

    public static EventStoreOptions mongo() {
        EventStoreOptions option = new EventStoreOptions();
        option.setDbName("enode");
        option.setCommandIdPattern(MONGO_PATTERN);
        option.setEventTableName("event_stream");
        option.setPublishedTableName("published_version");
        option.setEventVersionUkName("aggregateRootId_1_version_1");
        option.setEventCommandIdUkName("aggregateRootId_1_commandId_1");
        option.setPublishedUkName("processorName_1_aggregateRootId_1");
        return option;
    }

    public static EventStoreOptions pg() {
        EventStoreOptions option = new EventStoreOptions();
        option.setDbName("enode");
        option.setCommandIdPattern(PG_PATTERN);
        option.setEventTableName("event_stream");
        option.setPublishedTableName("published_version");
        option.setEventVersionUkName("uk_aggregate_root_id_version");
        option.setEventCommandIdUkName("uk_aggregate_root_id_command_id");
        option.setPublishedUkName("uk_processor_name_aggregate_root_id");
        return option;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getEventTableName() {
        return eventTableName;
    }

    public void setEventTableName(String eventTableName) {
        this.eventTableName = eventTableName;
    }

    public String getPublishedTableName() {
        return publishedTableName;
    }

    public void setPublishedTableName(String publishedTableName) {
        this.publishedTableName = publishedTableName;
    }

    public String getEventVersionUkName() {
        return eventVersionUkName;
    }

    public void setEventVersionUkName(String eventVersionUkName) {
        this.eventVersionUkName = eventVersionUkName;
    }

    public String getEventCommandIdUkName() {
        return eventCommandIdUkName;
    }

    public void setEventCommandIdUkName(String eventCommandIdUkName) {
        this.eventCommandIdUkName = eventCommandIdUkName;
    }

    public String getPublishedUkName() {
        return publishedUkName;
    }

    public void setPublishedUkName(String publishedUkName) {
        this.publishedUkName = publishedUkName;
    }

    public Pattern getCommandIdPattern() {
        return commandIdPattern;
    }

    public void setCommandIdPattern(Pattern commandIdPattern) {
        this.commandIdPattern = commandIdPattern;
    }

    public JsonObject getEventMeta() {
        return eventMeta;
    }

    public void setEventMeta(JsonObject eventMeta) {
        this.eventMeta = eventMeta;
    }

    public JsonObject getPublishedVersionMeta() {
        return publishedVersionMeta;
    }

    public void setPublishedVersionMeta(JsonObject publishedVersionMeta) {
        this.publishedVersionMeta = publishedVersionMeta;
    }
}
