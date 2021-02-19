package org.kamiblue.client.util.inventory

import net.minecraft.inventory.ClickType
import net.minecraft.inventory.Slot
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.util.delegate.ComputeFlag
import org.kamiblue.client.util.items.clickSlot

fun clickTask(block: ClickTask.Builder.() -> Unit) =
    ClickTask.Builder().apply(block).build()

class ClickTask private constructor(
    private val clicks: Array<ClickInfo>
) {
    val executed by ComputeFlag {
        index >= clicks.size
    }
    val confirmed by ComputeFlag {
        futures.all { it?.confirmed ?: false }
    }

    private val futures = arrayOfNulls<ClickFuture?>(clicks.size)
    private var index = 0

    fun timeout(timeout: Long) : Boolean {
        return confirmed || futures.all { it?.timeout(timeout) ?: false }
    }

    fun runTask(event: SafeClientEvent): ClickFuture? {
        if (index >= clicks.size) return null
        val currentIndex = index++
        return clicks[currentIndex].runClick(event).also { futures[currentIndex] = it }
    }

    class Builder {
        private val clicks = ArrayList<ClickInfo>()

        operator fun ClickInfo.unaryPlus() {
            clicks.add(this)
        }

        fun build() : ClickTask {
            return ClickTask(clicks.toTypedArray())
        }
    }
}

class ClickInfo (
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