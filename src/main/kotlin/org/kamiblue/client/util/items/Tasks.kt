package org.kamiblue.client.util.items

import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.util.TaskState
import org.kamiblue.client.util.threads.runSafeR
import net.minecraft.inventory.ClickType
import net.minecraft.inventory.Slot

class InventoryTask(
    private val id: Int,
    private val priority: Int,
    private val infoArray: Array<out ClickInfo>,
) : Comparable<InventoryTask> {

    val taskState: TaskState = TaskState()
    val isDone get() = taskState.done

    private var index: Int = 0

    fun nextInfo() =
        infoArray.getOrNull(index++).also {
            if (it == null) taskState.done = true
        }

    override fun compareTo(other: InventoryTask): Int {
        val result = priority - other.priority
        return if (result != 0) result
        else other.id - id
    }

    override fun equals(other: Any?) = this === other
        || (other is InventoryTask
        && id == other.id)

    override fun hashCode() = id

}

class ClickInfo(
    private val windowID: Int = 0,
    private val slot: Slot,
    private val mouseButton: Int = 0,
    private val type: ClickType
) {
    fun runClick() = runSafeR {
        val transactionID = clickSlot(windowID, slot, mouseButton, type)
        playerController.updateController()
        transactionID
    } ?: -32768

    fun runClick(event: SafeClientEvent): Short {
        val transactionID = event.clickSlot(windowID, slot, mouseButton, type)
        event.playerController.updateController()
        return transactionID
    }
}