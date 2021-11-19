package org.enodeframework.common.utils

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

/**
 * @author anruence@gmail.com
 */
object DateUtil {

    @JvmStatic
    fun parseDate(value: Any): Date {
        if (value is LocalDateTime) {
            return Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
        }
        if (value is LocalDate) {
            return Date.from(value.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        }
        if (value is Instant) {
            return Date.from(value)
        }
        if (value is Date) {
            return value
        }
        return Date()
    }
}