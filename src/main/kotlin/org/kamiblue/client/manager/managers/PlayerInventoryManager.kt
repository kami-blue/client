package org.kamiblue.client.manager.managers

import net.minecraft.client.gui.inventory.GuiContainer
import org.kamiblue.client.event.events.ConnectionEvent
import org.kamiblue.client.event.events.RenderOverlayEvent
import org.kamiblue.client.manager.Manager
import org.kamiblue.client.module.AbstractModule
import org.kamiblue.client.util.*
import org.kamiblue.client.util.items.ClickInfo
import org.kamiblue.client.util.items.InventoryTask
import org.kamiblue.client.util.items.removeHoldingItem
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.event.listener.listener
import java.util.*

object PlayerInventoryManager : Manager {
    private val timer = TickTimer()
    private val actionQueue = TreeSet<InventoryTask>(Comparator.reverseOrder())

    private var currentId = 0
    private var currentTask: InventoryTask? = null

    init {
        safeListener<RenderOverlayEvent>(0) {
            if ((currentTask == null && actionQueue.isEmpty())
                || !timer.tick((1000.0f / TpsCalculator.tickRate).toLong())) return@safeListener

            if (!player.inventory.itemStack.isEmpty) {
                if (mc.currentScreen is GuiContainer) {
                    timer.reset(250L) // Wait for 5 extra ticks if player is moving item
                } else {
                    removeHoldingItem()
                }

                return@safeListener
            }

            getTaskOrNext()?.nextInfo()?.runClick(this)

            if (actionQueue.isEmpty()) currentId = 0
        }

        listener<ConnectionEvent.Disconnect> {
            synchronized(this) {
                actionQueue.clear()
                currentId = 0
            }
        }
    }

    private fun getTaskOrNext() =
        currentTask?.takeIf {
            !it.isDone
        } ?: synchronized(this) {
            actionQueue.pollLast()?.also { currentTask = it }
        }

    /**
     * Adds a new task to the inventory manager
     *
     * @param clickInfo group of the click info in this task
     *
     * @return [TaskState] representing the state of this task
     */
    fun AbstractModule.addInventoryTask(vararg clickInfo: ClickInfo) =
        InventoryTask(currentId++, modulePriority, clickInfo).let {
            synchronized(this) {
                actionQueue.add(it)
            }
            it.taskState
        }

}