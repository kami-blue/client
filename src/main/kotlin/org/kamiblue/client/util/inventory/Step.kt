package org.kamiblue.client.util.inventory

import net.minecraft.inventory.ClickType
import net.minecraft.inventory.Slot
import org.kamiblue.client.event.SafeClientEvent

class Click(
    private val windowID: Int,
    private val slot: Slot,
    private val mouseButton: Int,
    private val type: ClickType
) {
    fun run(event: SafeClientEvent): ClickFuture {
        val id = event.clickSlot(windowID, slot, mouseButton, type)
        return ClickFuture(id)
    }
}

class ClickFuture(
    val id: Short,
) {
    private val time = System.currentTimeMillis()
    private var confirmed = false

    fun timeout(timeout: Long): Boolean {
        return confirmed || System.currentTimeMillis() - time > timeout
    }

    fun confirm() {
        confirmed = true
    }
}