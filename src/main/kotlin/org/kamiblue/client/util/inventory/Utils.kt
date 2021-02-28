package org.kamiblue.client.util.inventory

import kotlinx.coroutines.runBlocking
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.inventory.Container
import net.minecraft.inventory.Slot
import net.minecraft.network.play.client.CPacketClickWindow
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.util.inventory.slot.craftingSlots
import org.kamiblue.client.util.inventory.slot.firstItem
import org.kamiblue.client.util.inventory.slot.getSlots
import org.kamiblue.client.util.threads.onMainThreadSafe

/**
 * Put the item currently holding by mouse to somewhere or throw it
 */
fun SafeClientEvent.removeHoldingItem() {
    if (player.inventory.itemStack.isEmpty) return

    val slot = player.inventoryContainer.getSlots(9..45).firstItem(Items.AIR)?.slotNumber // Get empty slots in inventory and offhand
        ?: player.craftingSlots.firstItem(Items.AIR)?.slotNumber // Get empty slots in crafting slot
        ?: -999 // Throw on the ground

    clickSlot(0, slot, 0, ClickType.PICKUP)
}

/**
 * Performs inventory clicking in specific window, slot, mouseButton, and click type
 *
 * @return Transaction id
 */
fun SafeClientEvent.clickSlot(windowID: Int, slot: Slot, mouseButton: Int, type: ClickType): Short {
    return clickSlot(windowID, slot.slotNumber, mouseButton, type)
}

/**
 * Performs inventory clicking in specific window, slot, mouseButton, and click type
 *
 * @return Transaction id
 */
fun SafeClientEvent.clickSlot(windowID: Int, slot: Int, mouseButton: Int, type: ClickType): Short {
    val container = getContainerForID(windowID) ?: return -32768

    val playerInventory = player.inventory ?: return -32768
    val transactionID = container.getNextTransactionID(playerInventory)
    val itemStack = container.slotClick(slot, mouseButton, type, player)

    connection.sendPacket(CPacketClickWindow(windowID, slot, mouseButton, type, itemStack, transactionID))
    runBlocking {
        onMainThreadSafe { playerController.updateController() }
    }

    return transactionID
}

private fun SafeClientEvent.getContainerForID(windowID: Int): Container? =
    if (windowID == 0) player.inventoryContainer
    else player.openContainer