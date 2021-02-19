package org.kamiblue.client.util.inventory

import net.minecraft.inventory.ClickType
import net.minecraft.inventory.Slot
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.manager.managers.InventoryClickManager
import org.kamiblue.client.module.AbstractModule
import org.kamiblue.client.util.TimeUnit
import org.kamiblue.client.util.delegate.ComputeFlag
import org.kamiblue.client.util.items.clickSlot

fun SafeClientEvent.clickTaskNow(block: ClickTask.Builder.() -> Unit) =
    ClickTask.Builder().apply { priority(Int.MAX_VALUE) }.apply(block).build().also {
        InventoryClickManager.runNow(this, it)
    }

fun AbstractModule.clickTask(block: ClickTask.Builder.() -> Unit) =
    ClickTask.Builder().apply { priority(modulePriority) }.apply(block).build()

private fun clickTask(block: ClickTask.Builder.() -> Unit) =
    ClickTask.Builder().apply(block).build()

class ClickTask private constructor(
    private val id: Int,
    private val priority: Int,
    val delay: Long,
    val postDelay: Long,
    private val clicks: Array<ClickInfo>
) : Comparable<ClickTask> {
    val executed by ComputeFlag {
        cancelled || index >= clicks.size
    }
    val confirmed by ComputeFlag {
        cancelled || futures.all { it?.confirmed ?: false }
    }

    private val futures = arrayOfNulls<ClickFuture?>(clicks.size)
    private var index = 0
    private var cancelled = false

    fun timeout(timeout: Long): Boolean {
        return confirmed || futures.all { it?.timeout(timeout) ?: false }
    }

    fun runTask(event: SafeClientEvent): ClickFuture? {
        if (executed) return null
        val currentIndex = index++
        return clicks[currentIndex].runClick(event).also { futures[currentIndex] = it }
    }

    fun cancel() {
        cancelled = true
    }

    override fun compareTo(other: ClickTask): Int {
        return comparator.compare(this, other)
    }

    override fun equals(other: Any?) =
        this === other
            || (other is ClickTask
            && this.id == other.id)

    override fun hashCode() = id

    class Builder {
        private val clicks = ArrayList<ClickInfo>()
        private var priority = 0
        private var delay = 50L
        private var postDelay = 100L

        fun priority(value: Int) {
            priority = value
        }

        fun delay(value: Long, timeUnit: TimeUnit) {
            delay = value * timeUnit.multiplier
        }

        fun postDelay(value: Long, timeUnit: TimeUnit) {
            delay = value * timeUnit.multiplier
        }

        operator fun ClickInfo.unaryPlus() {
            clicks.add(this)
        }

        fun build(): ClickTask {
            return ClickTask(currentID++, priority, delay, postDelay, clicks.toTypedArray())
        }
    }

    private companion object {
        var currentID = Int.MIN_VALUE
        val comparator = compareByDescending<ClickTask> {
            it.priority
        }.thenBy {
            it.id
        }
    }
}

class ClickInfo(
    private val windowID: Int,
    private val slot: Slot,
    private val mouseButton: Int,
    private val type: ClickType
) {
    fun runClick(event: SafeClientEvent): ClickFuture {
        val id = event.clickSlot(windowID, slot, mouseButton, type)
        event.playerController.updateController()
        return ClickFuture(id)
    }
}

class ClickFuture(
    val id: Short,
) {
    val time = System.currentTimeMillis()
    var confirmed = false; private set

    fun timeout(timeout: Long): Boolean {
        return confirmed || System.currentTimeMillis() - time > timeout
    }

    fun confirm() {
        confirmed = true
    }

}