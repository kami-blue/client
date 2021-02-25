package org.kamiblue.client.util

open class Timer {
    var time = currentTime; protected set

    protected val currentTime get() = System.currentTimeMillis()

    fun reset(offset: Long = 0L) {
        time = currentTime + offset
    }

}

class TickTimer(val timeUnit: TimeUnit = TimeUnit.MILLISECONDS) : Timer() {
    fun tick(delay: Int, resetIfTick: Boolean = true): Boolean {
        return tick(delay.toLong(), resetIfTick)
    }

    fun tick(delay: Long, resetIfTick: Boolean = true): Boolean {
        return if (currentTime - time > delay * timeUnit.multiplier) {
            if (resetIfTick) time = currentTime
            true
        } else {
            false
        }
    }
}

class StopTimer(val timeUnit: TimeUnit = TimeUnit.MILLISECONDS) : Timer() {
    fun stop(): Long {
        return (currentTime - time) / timeUnit.multiplier
    }
}

enum class TimeUnit(val multiplier: Long) {
    MILLISECONDS(1L),
    TICKS(50L),
    SECONDS(1000L),
    MINUTES(60000L);
}