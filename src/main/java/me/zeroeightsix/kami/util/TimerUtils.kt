package me.zeroeightsix.kami.util

/**
 * @author Xiaro
 *
 * Created by Xiaro on 26/08/20
 * Updated by Xiaro on 09/09/20
 */
open class TimerUtils {
    var time = getCurrentTime()
        protected set

    protected fun getCurrentTime(): Long {
        return System.currentTimeMillis()
    }

    fun reset(offset: Long = 0L) {
        time = getCurrentTime() + offset
    }

    enum class TimeUnit(val multiplier: Long) {
        MILLISECONDS(1L),
        TICKS(50L),
        SECONDS(1000L),
        MINUTES(60000L);
    }


    // Implementations
    class TickTimer(val timeUnit: TimeUnit = TimeUnit.MILLISECONDS) : TimerUtils() {
        fun tick(delay: Long, resetIfTick: Boolean = true): Boolean {
            return if (getCurrentTime() - time > delay * timeUnit.multiplier) {
                if (resetIfTick) time = getCurrentTime()
                true
            } else {
                false
            }
        }
    }

    class StopTimer(val timeUnit: TimeUnit = TimeUnit.MILLISECONDS) : TimerUtils() {
        fun stop(): Long {
            return (getCurrentTime() - time) / timeUnit.multiplier
        }
    }
}