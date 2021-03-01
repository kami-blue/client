@file:Suppress("NOTHING_TO_INLINE") // Looks like inlining stuff here reduced the size of compiled code
package org.kamiblue.client.util.inventory.operation

import net.minecraft.inventory.ClickType
import net.minecraft.inventory.Slot
import org.kamiblue.client.util.inventory.Click
import org.kamiblue.client.util.inventory.InventoryTask
import org.kamiblue.client.util.inventory.slot.HotbarSlot

inline fun InventoryTask.Builder.pickUp(slot: Slot) {
    pickUp(0, slot)
}

inline fun InventoryTask.Builder.pickUp(windowID: Int, slot: Slot) {
    + Click(windowID, slot, 0, ClickType.PICKUP)
}

inline fun InventoryTask.Builder.pickUpAll(slot: Slot) {
    pickUpAll(0, slot)
}

inline fun InventoryTask.Builder.pickUpAll(windowID: Int, slot: Slot) {
    + Click(windowID, slot, 0, ClickType.PICKUP_ALL)
}

inline fun InventoryTask.Builder.quickMove(slot: Slot) {
    quickMove(0, slot)
}

inline fun InventoryTask.Builder.quickMove(windowID: Int, slot: Slot) {
    + Click(windowID, slot, 0, ClickType.QUICK_MOVE)
}

inline fun InventoryTask.Builder.swapWith(slot: Slot, hotbarSlot: HotbarSlot) {
    swapWith(0, slot, hotbarSlot)
}

inline fun InventoryTask.Builder.swapWith(windowID: Int, slot: Slot, hotbarSlot: HotbarSlot) {
    + Click(windowID, slot, hotbarSlot.hotbarSlot, ClickType.SWAP)
}

inline fun InventoryTask.Builder.throwOne(slot: Slot) {
    throwOne(0, slot)
}

inline fun InventoryTask.Builder.throwOne(windowID: Int, slot: Slot) {
    + Click(windowID, slot, 0, ClickType.THROW)
}

inline fun InventoryTask.Builder.throwAll(slot: Slot) {
    throwAll(0, slot)
}

inline fun InventoryTask.Builder.throwAll(windowID: Int, slot: Slot) {
    + Click(windowID, slot, 1, ClickType.THROW)
}