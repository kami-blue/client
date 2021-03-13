package org.kamiblue.client.util

class TimeoutFlag<T> private constructor(
    val value: T,
    private val timeoutTime: Long
) {
    fun timeout() =
        System.currentTimeMillis() > timeoutTime

    companion object {
        fun <T> relative(value: T, timeout: Long) =
            TimeoutFlag(value, System.currentTimeMillis() + timeout)

        fun <T> absolute(value: T, timeout: Long) =
            TimeoutFlag(value, timeout)
    }
}