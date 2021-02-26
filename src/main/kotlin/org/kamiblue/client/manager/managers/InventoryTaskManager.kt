package org.kamiblue.client.manager.managers

import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.network.play.server.SPacketConfirmTransaction
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.ConnectionEvent
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.event.events.RunGameLoopEvent
import org.kamiblue.client.manager.Manager
import org.kamiblue.client.util.TickTimer
import org.kamiblue.client.util.TpsCalculator
import org.kamiblue.client.util.inventory.ClickFuture
import org.kamiblue.client.util.inventory.InventoryTask
import org.kamiblue.client.util.inventory.TaskFuture
import org.kamiblue.client.util.items.removeHoldingItem
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.event.listener.listener
import java.util.*
import kotlin.collections.HashMap

object InventoryTaskManager : Manager {
    private val confirmMap = HashMap<Short, ClickFuture>()
    private val taskQueue = PriorityQueue<InventoryTask>()
    private val timer = TickTimer()
    private var lastTask: InventoryTask? = null

    init {
        listener<PacketEvent.Receive> {
            if (it.packet !is SPacketConfirmTransaction) return@listener
            synchronized(InventoryTaskManager) {
                confirmMap.remove(it.packet.actionNumber)?.confirm()
            }
        }

        safeListener<RunGameLoopEvent.Render> {
            if (lastTask == null && taskQueue.isEmpty()) return@safeListener
            if (!timer.tick(0L, false)) return@safeListener

            lastTaskOrNext()?.let {
                runTask(it)
            }
        }

        listener<ConnectionEvent.Disconnect> {
            reset()
        }
    }

    fun addTask(task: InventoryTask) {
        synchronized(InventoryTaskManager) {
            taskQueue.add(task)
        }
    }

    fun runNow(event: SafeClientEvent, task: InventoryTask) {
        while (!task.executed) {
            task.runTask(event)?.let {
                handleFuture(it)
            }
        }
        timer.reset((task.postDelay * TpsCalculator.multiplier).toLong())
    }

    private fun SafeClientEvent.lastTaskOrNext(): InventoryTask? {
        return lastTask ?: run {
            val newTask = synchronized(InventoryTaskManager) {
                taskQueue.poll()?.also { lastTask = it }
            } ?: return null

            if (!player.inventory.itemStack.isEmpty && mc.currentScreen !is GuiContainer) {
                removeHoldingItem()
                return null
            }

            newTask
        }
    }

    private fun SafeClientEvent.runTask(task: InventoryTask) {
        if (!player.inventory.itemStack.isEmpty && !task.runInGui && mc.currentScreen is GuiContainer) {
            timer.reset(500L)
            return
        }

        if (task.delay == 0L) {
            runNow(this, task)
        } else {
            task.runTask(this)?.let {
                handleFuture(it)
                timer.reset((task.delay * TpsCalculator.multiplier).toLong())
            }
        }

        if (task.finished) {
            timer.reset((task.postDelay * TpsCalculator.multiplier).toLong())
            lastTask = null
            return
        }
    }

    private fun handleFuture(future: TaskFuture) {
        if (future !is ClickFuture) return

        synchronized(InventoryTaskManager) {
            confirmMap[future.id] = future
        }
    }

    private fun reset() {
        synchronized(InventoryTaskManager) {
            confirmMap.clear()
            lastTask?.cancel()
            lastTask = null
            taskQueue.clear()
        }
    }

}