package org.kamiblue.client.manager.managers

import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.network.play.server.SPacketConfirmTransaction
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.ConnectionEvent
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.event.events.RenderEvent
import org.kamiblue.client.manager.Manager
import org.kamiblue.client.util.TickTimer
import org.kamiblue.client.util.TpsCalculator
import org.kamiblue.client.util.inventory.ClickFuture
import org.kamiblue.client.util.inventory.ClickTask
import org.kamiblue.client.util.items.removeHoldingItem
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.event.listener.listener
import java.util.*
import kotlin.collections.HashMap

internal object InventoryClickManager : Manager {
    private val confirmMap = HashMap<Short, ClickFuture>()
    private val taskQueue = PriorityQueue<ClickTask>()
    private val timer = TickTimer()
    private var lastTask: ClickTask? = null

    init {
        listener<PacketEvent.Receive> {
            if (it.packet !is SPacketConfirmTransaction) return@listener
            synchronized(InventoryClickManager) {
                confirmMap.remove(it.packet.actionNumber)?.confirm()
            }
        }

        safeListener<RenderEvent> {
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

    fun addTask(task: ClickTask) {
        synchronized(InventoryClickManager) {
            taskQueue.add(task)
        }
    }

    fun runNow(event: SafeClientEvent, task: ClickTask) {
        while (!task.executed) {
            task.runTask(event)?.addToMap()
        }
        timer.reset((task.postDelay * TpsCalculator.multiplier).toLong())
    }

    private fun SafeClientEvent.lastTaskOrNext(): ClickTask? {
        return lastTask ?: run {
            val newTask = synchronized(InventoryClickManager) {
                taskQueue.poll()?.also { lastTask = it }
            } ?: return null

            if (!player.inventory.itemStack.isEmpty && mc.currentScreen !is GuiContainer) {
                removeHoldingItem()
                return null
            }

            newTask
        }
    }

    private fun SafeClientEvent.runTask(task: ClickTask) {
        if (!player.inventory.itemStack.isEmpty && !task.runInGui && mc.currentScreen is GuiContainer) {
            timer.reset(500L)
            return
        }

        if (task.delay == 0L) {
            runNow(this, task)
        } else {
            task.runTask(this)?.addToMap()
            timer.reset((task.delay * TpsCalculator.multiplier).toLong())
        }

        if (task.executed) {
            timer.reset((task.postDelay * TpsCalculator.multiplier).toLong())
            lastTask = null
            return
        }
    }

    private fun ClickFuture.addToMap() {
        synchronized(InventoryClickManager) {
            confirmMap[this.id] = this
        }
    }

    private fun reset() {
        synchronized(InventoryClickManager) {
            confirmMap.clear()
            lastTask?.cancel()
            lastTask = null
            taskQueue.clear()
        }
    }

}