package org.kamiblue.client.util

class TimedFlag<T : Comparable<*>>(valueIn: T) {
    var value = valueIn
        set(value) {
            lastUpdateTime = System.currentTimeMillis()
            field = value
        }
    var lastUpdateTime = 0L
        private set
}