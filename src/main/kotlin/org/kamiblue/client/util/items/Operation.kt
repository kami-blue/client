package org.kamiblue.client.util.items

import kotlinx.coroutines.runBlocking
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.util.threads.onMainThreadSafe
import net.minecraft.block.Block
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.inventory.Container
import net.minecraft.inventory.Slot
import net.minecraft.network.play.client.CPacketClickWindow

/**
 * Move the item in [slotFrom]  to [slotTo] in player inventory,
 * if [slotTo] contains an item, then move it to [slotFrom]
 */
fun SafeClientEvent.moveToSlot(slotFrom: Slot, slotTo: Slot): ShortArray {
    return moveToSlot(0, slotFrom, slotTo)
}

/**
 * Move the item in [slotFrom] to [slotTo] in [windowID],
 * if [slotTo] contains an item, then move it to [slotFrom]
 */
fun SafeClientEvent.moveToSlot(windowID: Int, slotFrom: Slot, slotTo: Slot): ShortArray {
    return shortArrayOf(
        clickSlot(windowID, slotFrom, type = ClickType.PICKUP),
        clickSlot(windowID, slotTo, type = ClickType.PICKUP),
        clickSlot(windowID, slotFrom, type = ClickType.PICKUP)
    )
}

/**
 * Move all the item that equals to the item in [slotTo] to [slotTo] in player inventory
 */
fun SafeClientEvent.moveAllToSlot(slotTo: Slot): ShortArray {
    return shortArrayOf(
        clickSlot(slot = slotTo, type = ClickType.PICKUP_ALL),
        clickSlot(slot = slotTo, type = ClickType.PICKUP)
    )
}

/**
 * Quick move (Shift + Click) the item in [slot] in player inventory
 */
fun SafeClientEvent.quickMoveSlot(slot: Slot): Short {
    return quickMoveSlot(0, slot)
}

/**
 * Quick move (Shift + Click) the item in [slot] in specified [windowID]
 */
fun SafeClientEvent.quickMoveSlot(windowID: Int, slot: Slot): Short {
    return clickSlot(windowID, slot, type = ClickType.QUICK_MOVE)
}


/**
 * Throw all the item in [slot] in player inventory
 */
fun SafeClientEvent.throwAllInSlot(slot: Slot): Short {
    return throwAllInSlot(0, slot)
}

/**
 * Throw all the item in [slot] in specified [windowID]
 */
fun SafeClientEvent.throwAllInSlot(windowID: Int, slot: Slot): Short {
    return clickSlot(windowID, slot, 1, ClickType.THROW)
}

/**
 * Put the item currently holding by mouse to somewhere or throw it
 */
fun SafeClientEvent.removeHoldingItem() {
    if (player.inventory.itemStack.isEmpty) return

    val slot = player.inventoryContainer.getSlots(9..45).firstItem(Items.AIR)?.slotNumber // Get empty slots in inventory and offhand
        ?: player.craftingSlots.firstItem(Items.AIR)?.slotNumber // Get empty slots in crafting slot
        ?: -999 // Throw on the ground

    clickSlot(slot = slot, type = ClickType.PICKUP)
}


/**
 * Performs inventory clicking in specific window, slot, mouseButton, and click type
 *
 * @return Transaction id
 */
fun SafeClientEvent.clickSlot(windowID: Int = 0, slot: Slot, mouseButton: Int = 0, type: ClickType): Short {
    return clickSlot(windowID, slot.slotNumber, mouseButton, type)
}

/**
 * Performs inventory clicking in specific window, slot, mouseButton, and click type
 *
 * @return Transaction id
 */
fun SafeClientEvent.clickSlot(windowID: Int = 0, slot: Int, mouseButton: Int = 0, type: ClickType): Short {
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

fun SafeClientEvent.getContainerForID(windowID: Int): Container? =
    if (windowID == 0) player.inventoryContainer
    else player.openContainer