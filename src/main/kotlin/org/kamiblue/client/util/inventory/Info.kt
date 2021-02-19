package org.kamiblue.client.util.inventory

import net.minecraft.inventory.ClickType
import net.minecraft.inventory.Slot
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.util.items.clickSlot

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