package org.kamiblue.client.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import kotlin.collections.HashMap

object TimeUtils {

    private val formatterMap = HashMap<Pair<TimeFormat, TimeUnit>, DateTimeFormatter>()

    fun getDate(): String = LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))

    fun getDate(dateFormat: DateFormat): String = LocalDate.now().format(dateFormat.formatter)

    fun getTime(): String = LocalTime.now().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

    fun getTime(timeFormat: TimeFormat, timeUnit: TimeUnit): String = LocalTime.now().format(timeFormat.getFormatter(timeUnit))

    private fun TimeFormat.getFormatter(timeUnit: TimeUnit) =
        formatterMap.getOrPut(this to timeUnit) {
            val pattern = if (timeUnit == TimeUnit.H24) pattern.replace('h', 'H') else "$pattern a"
            DateTimeFormatter.ofPattern(pattern, Locale.US)
        }

    @Suppress("UNUSED")
    enum class DateFormat(val formatter: DateTimeFormatter) {
        DDMMYY(DateTimeFormatter.ofPattern("dd/MM/yy")),
        YYMMDD(DateTimeFormatter.ofPattern("yy/MM/dd")),
        MMDDYY(DateTimeFormatter.ofPattern("MM/dd/yy"))
    }

    @Suppress("UNUSED")
    enum class TimeFormat(val pattern: String) {
        HHMMSS("hh:mm:ss"),
        HHMM("hh:mm"),
        HH("hh")
    }

    @Suppress("UNUSED")
    enum class TimeUnit {
        H12, H24
    }

}