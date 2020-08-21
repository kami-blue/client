package me.zeroeightsix.kami.util

open class TimerUtils {

    protected fun getCurrentTime(): Long {
        return System.currentTimeMillis()
    }

    class TickTimer(val timeUnit: TimeUnit = TimeUnit.MILLISECONDS) : TimerUtils() {
        var lastTickTime = getCurrentTime()

        fun tick(delay: Long): Boolean {
            return if (getCurrentTime() - lastTickTime > delay * timeUnit.multiplier) {
                lastTickTime = getCurrentTime()
                true
            } else {
                false
            }
        }
    }

    class StopTimer(val timeUnit: TimeUnit = TimeUnit.MILLISECONDS) : TimerUtils() {
        private val startTime: Long = getCurrentTime()

        fun stop(): Long {
            return (getCurrentTime() - startTime) * timeUnit.multiplier
        }
    }

    enum class TimeUnit(val multiplier: Long) {
        MILLISECONDS(1L),
        SECONDS(1000L),
        MINUTES(60000L);
    }
}