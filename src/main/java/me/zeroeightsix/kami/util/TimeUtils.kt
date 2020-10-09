package me.zeroeightsix.kami.util

import net.minecraft.util.text.TextFormatting
import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    /**
     * Get current time
     */
    fun time(format: SimpleDateFormat): String {
        val date = Date(System.currentTimeMillis())
        return format.format(date)
    }

    private fun formatTimeString(timeType: TimeType): String {
        return when (timeType) {
            TimeType.HHMM -> ":mm"
            TimeType.HHMMSS -> ":mm:ss"
            else -> ""
        }
    }

    fun dateFormatter(timeUnit: TimeUnit, timeType: TimeType): SimpleDateFormat {
        return when (timeUnit) {
            TimeUnit.H12 -> SimpleDateFormat("hh" + formatTimeString(timeType), Locale.UK)
            TimeUnit.H24 -> SimpleDateFormat("HH" + formatTimeString(timeType), Locale.UK)
        }
    }

    fun getFinalTime(colourCode2: TextFormatting, colourCode1: TextFormatting, timeUnit: TimeUnit, timeType: TimeType, doLocale: Boolean): String {
        val time = time(dateFormatter(TimeUnit.H24, TimeType.HH))
        val locale = if (timeUnit == TimeUnit.H12 && doLocale) {
            // checks if the 24 hour time minus 12 is negative or 0, if it is it's pm
            if (time.toInt() - 12 >= 0) " pm"
            else " am"
        } else ""
        return colourCode1.toString() + time(dateFormatter(timeUnit, timeType)) + colourCode2.toString() + locale
    }

    fun getFinalTime(timeUnit: TimeUnit, timeType: TimeType, doLocale: Boolean): String {
        var locale = ""
        val time = time(dateFormatter(TimeUnit.H24, TimeType.HH))
        if (timeUnit == TimeUnit.H12 && doLocale) {
            locale = if (time.toInt() - 12 >= 0) { // checks if the 24 hour time minus 12 is negative or 0, if it is it's pm
                "pm"
            } else {
                "am"
            }
        }
        return time(dateFormatter(timeUnit, timeType)) + locale
    }

    enum class TimeType {
        HHMM, HHMMSS, HH
    }

    enum class TimeUnit {
        H24, H12
    }
}