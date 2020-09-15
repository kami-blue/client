package me.zeroeightsix.kami.util

import me.zeroeightsix.kami.module.modules.client.InfoOverlay
import net.minecraft.util.text.TextFormatting
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author dominikaaaa
 * Updated by dominikaaaa on 06/02/20
 * Updated by Xiaro on 18/08/20
 */
object TimeUtils {
    /**
     * Get current time
     */
    @JvmStatic
    fun time(format: SimpleDateFormat): String {
        val date = Date(System.currentTimeMillis())
        return format.format(date)
    }

    @JvmStatic
    private fun formatTimeString(timeType: TimeType): String {
        return when (timeType) {
            TimeType.HHMM -> ":mm"
            TimeType.HHMMSS -> ":mm:ss"
            else -> ""
        }
    }

    @JvmStatic
    fun dateFormatter(timeUnit: TimeUnit, timeType: TimeType): SimpleDateFormat {
        return when (timeUnit) {
            TimeUnit.H12 -> SimpleDateFormat("hh" + formatTimeString(timeType), Locale.UK)
            TimeUnit.H24 -> SimpleDateFormat("HH" + formatTimeString(timeType), Locale.UK)
        }
    }

    @JvmStatic
    fun getFinalTime(colourCode2: TextFormatting, colourCode1: TextFormatting, timeUnit: TimeUnit, timeType: TimeType, doLocale: Boolean): String {
        var locale = ""
        val time = time(dateFormatter(TimeUnit.H24, TimeType.HH))
        if (timeUnit == TimeUnit.H12 && doLocale) {
            locale = if (time.toInt() - 12 >= 0) { // checks if the 24 hour time minus 12 is negative or 0, if it is it's pm
                "pm"
            } else {
                "am"
            }
        }
        return colourCode1.toString() + time(dateFormatter(timeUnit, timeType)) + colourCode2.toString() + locale
    }

    @JvmStatic
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