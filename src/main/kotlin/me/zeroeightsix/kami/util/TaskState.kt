package me.zeroeightsix.kami.util

class TaskState(done: Boolean = false) {
    var done = done
        set(_) {
            field = true
        }
}