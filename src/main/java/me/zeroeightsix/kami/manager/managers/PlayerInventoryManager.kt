package me.zeroeightsix.kami.manager.managers

import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.RenderOverlayEvent
import me.zeroeightsix.kami.manager.Manager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.*
import me.zeroeightsix.kami.util.items.clickSlot
import me.zeroeightsix.kami.util.items.getContainerForID
import me.zeroeightsix.kami.util.items.removeHoldingItem
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.ClickType
import org.kamiblue.event.listener.listener
import java.util.*

object PlayerInventoryManager : Manager {
    private val timer = TickTimer()
    private val lockObject = Any()
    private val actionQueue = TreeSet<InventoryTask>(Comparator.reverseOrder())

    private var currentId = 0
    private var currentTask: InventoryTask? = null

    init {
        safeListener<RenderOverlayEvent>(0) {
            if (currentTask == null || actionQueue.isEmpty()
                || !timer.tick((1000.0f / TpsCalculator.tickRate).toLong())) return@safeListener

            if (!player.inventory.itemStack.isEmpty) {
                if (mc.currentScreen is GuiContainer) {
                    timer.reset(250L) // Wait for 5 extra ticks if player is moving item
                } else {
                    removeHoldingItem()
                }

                return@safeListener
            }

            getTaskOrNext()?.nextInfo()?.let {
                val slot = getContainerForID(it.windowID)?.getSlot(it.slot) ?: return@let

                clickSlot(it.windowID, slot, it.mouseButton, it.type)
                playerController.updateController()
            }

            if (actionQueue.isEmpty()) currentId = 0
        }

        listener<ConnectionEvent.Disconnect> {
            actionQueue.clear()
            currentId = 0
        }
    }

    private fun getTaskOrNext() =
        currentTask?.takeIf {
            !it.isDone
        } ?: synchronized(lockObject) {
            actionQueue.removeIf { it.isDone }
            actionQueue.firstOrNull()
        }

    /**
     * Adds a new task to the inventory manager
     *
     * @param clickInfo group of the click info in this task
     *
     * @return [TaskState] representing the state of this task
     */
    fun Module.addInventoryTask(vararg clickInfo: ClickInfo) =
        InventoryTask(currentId++, modulePriority, clickInfo).let {
            actionQueue.add(it)
            it.taskState
        }

    private class InventoryTask(
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

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is InventoryTask) return false

            if (!infoArray.contentEquals(other.infoArray)) return false
            if (index != other.index) return false

            return true
        }

        override fun hashCode() = 31 * infoArray.contentHashCode() + index

    }

    class ClickInfo(val windowID: Int = 0, val slot: Int, val mouseButton: Int = 0, val type: ClickType)
}