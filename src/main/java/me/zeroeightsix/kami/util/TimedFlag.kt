package me.zeroeightsix.kami.util

class TimedFlag<T : Comparable<*>>(valueIn: T) {
    var value = valueIn
        set(value) {
            if (value != field) {
                lastUpdateTime = System.currentTimeMillis()
                field = value
            }
        }
    var lastUpdateTime = System.currentTimeMillis()
        private set
}