package org.kamiblue.client.util.inventory

import net.minecraft.inventory.ClickType
import net.minecraft.inventory.Slot
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.util.inventory.clickSlot

class SwapSlot(
    private val slot: Int
) : TaskStep {
    override fun run(event: SafeClientEvent): TaskFuture {
        event.player.inventory.currentItem = slot
        event.playerController.updateController()
        return ImmediateFuture()
    }
}

class ImmediateFuture : TaskFuture {
    override fun timeout(timeout: Long): Boolean {
        return true
    }
}

class Click(
    private val windowID: Int,
    private val slot: Slot,
    private val mouseButton: Int,
    private val type: ClickType
) : TaskStep {
    override fun run(event: SafeClientEvent): TaskFuture {
        val id = event.clickSlot(windowID, slot, mouseButton, type)
        return ClickFuture(id)
    }
}

class ClickFuture(
    val id: Short,
) : TaskFuture {
    private val time = System.currentTimeMillis()
    private var confirmed = false

    override fun timeout(timeout: Long): Boolean {
        return confirmed || System.currentTimeMillis() - time > timeout
    }

    fun confirm() {
        confirmed = true
    }
}

interface TaskStep {
    fun run(event: SafeClientEvent): TaskFuture
}

interface TaskFuture {
    fun timeout(timeout: Long): Boolean
}