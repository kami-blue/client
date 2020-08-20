package me.zeroeightsix.kami.util

class Timer(val timeUnit: TimeUnit = TimeUnit.MILLISECONDS) {

    private val currentTime: Long get() = System.currentTimeMillis()
    @JvmField var lastTickTime: Long = currentTime

    fun tick(delay: Long): Boolean {
        return if (currentTime - lastTickTime > delay * timeUnit.multiplier) {
            lastTickTime = currentTime
            true
        } else {
            false
        }
    }

    enum class TimeUnit(val multiplier: Long) {
        MILLISECONDS(1L),
        SECONDS(1000L),
        MINUTES(60000L);
    }
}