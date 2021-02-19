package org.kamiblue.client.util.inventory

import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.manager.managers.InventoryTaskManager
import org.kamiblue.client.module.AbstractModule
import org.kamiblue.client.util.TimeUnit
import org.kamiblue.client.util.delegate.ComputeFlag

fun SafeClientEvent.inventoryTaskNow(block: InventoryTask.Builder.() -> Unit) =
    InventoryTask.Builder().apply { priority(Int.MAX_VALUE) }.apply(block).build().also {
        InventoryTaskManager.runNow(this, it)
    }

fun AbstractModule.inventoryTask(block: InventoryTask.Builder.() -> Unit) =
    InventoryTask.Builder().apply { priority(modulePriority) }.apply(block).build().also {
        InventoryTaskManager.addTask(it)
    }

val InventoryTask?.executedOrTrue get() = this == null || this.executed

val InventoryTask?.confirmedOrTrue get() = this == null || this.confirmed

class InventoryTask private constructor(
    private val id: Int,
    private val priority: Int,
    val delay: Long,
    val postDelay: Long,
    val timeout: Long,
    val runInGui: Boolean,
    private val clicks: Array<TaskStep>
) : Comparable<InventoryTask> {
    val executed by ComputeFlag {
        cancelled || finishTime != -1L && System.currentTimeMillis() - finishTime > postDelay
    }
    val confirmed by ComputeFlag {
        cancelled || executed && futures.all { it?.timeout(timeout) ?: true }
    }

    private val futures = arrayOfNulls<TaskFuture?>(clicks.size)
    private var finishTime = -1L
    private var index = 0
    private var cancelled = false

    fun runTask(event: SafeClientEvent): TaskFuture? {
        if (cancelled || index >= clicks.size) {
            if (finishTime == -1L) {
                event.playerController.updateController()
                finishTime = System.currentTimeMillis()
            }

            return null
        }

        val currentIndex = index++
        val future = clicks[currentIndex].run(event)
        futures[currentIndex] = future

        return future
    }

    fun cancel() {
        cancelled = true
    }

    override fun compareTo(other: InventoryTask): Int {
        return comparator.compare(this, other)
    }

    override fun equals(other: Any?) =
        this === other
            || (other is InventoryTask
            && this.id == other.id)

    override fun hashCode() = id

    class Builder {
        private val infos = ArrayList<TaskStep>()
        private var priority = 0
        private var delay = 50L
        private var postDelay = 100L
        private var timeout = 3000L
        private var runInGui = false

        fun priority(value: Int) {
            priority = value
        }

        fun delay(value: Long, timeUnit: TimeUnit = TimeUnit.MILLISECONDS) {
            delay = value * timeUnit.multiplier
        }

        fun postDelay(value: Long, timeUnit: TimeUnit = TimeUnit.MILLISECONDS) {
            postDelay = value * timeUnit.multiplier
        }

        fun timeout(value: Long, timeUnit: TimeUnit = TimeUnit.MILLISECONDS) {
            timeout = value * timeUnit.multiplier
        }

        fun runInGui() {
            runInGui = true
        }

        operator fun TaskStep.unaryPlus() {
            infos.add(this)
        }

        fun build(): InventoryTask {
            return InventoryTask(currentID++, priority, delay, postDelay, timeout, runInGui, infos.toTypedArray())
        }
    }

    private companion object {
        var currentID = Int.MIN_VALUE
        val comparator = compareByDescending<InventoryTask> {
            it.priority
        }.thenBy {
            it.id
        }
    }
}