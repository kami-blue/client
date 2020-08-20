package me.zeroeightsix.kami.util

open class TimerUtils {

    protected val currentTime: Long get() = System.currentTimeMillis()

    class TickTimer(val timeUnit: TimeUnit = TimeUnit.MILLISECONDS) : TimerUtils() {
        var lastTickTime = currentTime

        fun tick(delay: Long): Boolean {
            return if (currentTime - lastTickTime > delay * timeUnit.multiplier) {
                lastTickTime = currentTime
                true
            } else {
                false
            }
        }
    }

    class StopTimer(val timeUnit: TimeUnit = TimeUnit.MILLISECONDS) : TimerUtils() {
        private val startTime: Long = currentTime

        fun stop(): Long {
            return (currentTime - startTime) * timeUnit.multiplier
        }
    }

    enum class TimeUnit(val multiplier: Long) {
        MILLISECONDS(1L),
        SECONDS(1000L),
        MINUTES(60000L);
    }
}