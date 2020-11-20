package org.enodeframework.mysql

import org.enodeframework.common.serializing.ISerializeService
import org.enodeframework.eventing.IEventSerializer
import org.enodeframework.jdbc.DBConfiguration
import org.enodeframework.jdbc.JDBCEventStore
import java.util.regex.Pattern
import javax.sql.DataSource

/**
 * @author anruence@gmail.com
 */
class MysqlEventStore(dataSource: DataSource, setting: DBConfiguration, eventSerializer: IEventSerializer, serializeService: ISerializeService) : JDBCEventStore(dataSource, setting, eventSerializer, serializeService) {

    public override fun parseCommandId(msg: String): String {
        val matcher = PATTERN_MYSQL.matcher(msg)
        if (!matcher.find()) {
            return ""
        }
        return if (matcher.groupCount() == 0) {
            ""
        } else matcher.group(1)
    }

    companion object {
        private val PATTERN_MYSQL = Pattern.compile("^Duplicate entry '.*-(.*)' for key")
    }
}