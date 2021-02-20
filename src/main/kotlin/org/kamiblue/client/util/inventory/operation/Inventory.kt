package org.kamiblue.client.util.inventory.operation

import net.minecraft.inventory.Slot
import org.kamiblue.client.util.inventory.InventoryTask

/**
 * Move the item in [slotFrom]  to [slotTo] in player inventory,
 * if [slotTo] contains an item, then move it to [slotFrom]
 */
fun InventoryTask.Builder.moveTo(slotFrom: Slot, slotTo: Slot) {
    moveTo(0, slotFrom, slotTo)
}

/**
 * Move the item in [slotFrom] to [slotTo] in [windowID],
 * if [slotTo] contains an item, then move it to [slotFrom]
 */
fun InventoryTask.Builder.moveTo(windowID: Int, slotFrom: Slot, slotTo: Slot) {
    pickUp(windowID, slotFrom)
    pickUp(windowID, slotTo)
    pickUp(windowID, slotFrom)
}

/**
 * Move all the item that equals to the item in [slotTo] to [slotTo] in player inventory
 */
fun InventoryTask.Builder.moveAllTo(slotTo: Slot) {
    moveAllTo(0, slotTo)
}

/**
 * Move all the item that equals to the item in [slotTo] to [slotTo] in [windowID]
 */
fun InventoryTask.Builder.moveAllTo(windowID: Int, slotTo: Slot) {
    pickUp(windowID, slotTo)
    pickUpAll(windowID, slotTo)
    pickUp(windowID, slotTo)
}