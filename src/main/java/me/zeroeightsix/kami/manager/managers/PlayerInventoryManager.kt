package me.zeroeightsix.kami.manager.managers

import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.RenderOverlayEvent
import me.zeroeightsix.kami.manager.Manager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.*
import me.zeroeightsix.kami.util.items.*
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.client.gui.inventory.GuiContainer
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
    fun Module.addInventoryTask(vararg clickInfo: ClickInfo) =
        InventoryTask(currentId++, modulePriority, clickInfo).let {
            synchronized(this) {
                actionQueue.add(it)
            }
            it.taskState
        }

}