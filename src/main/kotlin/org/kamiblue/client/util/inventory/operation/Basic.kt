package org.kamiblue.client.util.inventory.operation

import net.minecraft.inventory.ClickType
import net.minecraft.inventory.Slot
import org.kamiblue.client.util.inventory.Click
import org.kamiblue.client.util.inventory.InventoryTask
import org.kamiblue.client.util.inventory.SwapSlot
import org.kamiblue.client.util.inventory.slot.HotbarSlot

fun InventoryTask.Builder.swapSlot(slot: HotbarSlot) {
    + SwapSlot(slot.hotbarSlot)
}

fun InventoryTask.Builder.swapSlot(slot: Int) {
    if (slot !in 0..8) return
    + SwapSlot(slot)
}

fun InventoryTask.Builder.pickUp(slot: Slot) {
    pickUp(0, slot)
}

fun InventoryTask.Builder.pickUp(windowID: Int, slot: Slot) {
    + Click(windowID, slot, 0, ClickType.PICKUP)
}

fun InventoryTask.Builder.pickUpAll(slot: Slot) {
    pickUpAll(0, slot)
}

fun InventoryTask.Builder.pickUpAll(windowID: Int, slot: Slot) {
    + Click(windowID, slot, 0, ClickType.PICKUP_ALL)
}

fun InventoryTask.Builder.quickMove(slot: Slot) {
    quickMove(0, slot)
}

fun InventoryTask.Builder.quickMove(windowID: Int, slot: Slot) {
    + Click(windowID, slot, 0, ClickType.QUICK_MOVE)
}

fun InventoryTask.Builder.swapWith(slot: Slot, hotbarSlot: HotbarSlot) {
    swapWith(0, slot, hotbarSlot)
}

fun InventoryTask.Builder.swapWith(windowID: Int, slot: Slot, hotbarSlot: HotbarSlot) {
    + Click(windowID, slot, hotbarSlot.hotbarSlot, ClickType.SWAP)
}

fun InventoryTask.Builder.throwOne(slot: Slot) {
    throwOne(0, slot)
}

fun InventoryTask.Builder.throwOne(windowID: Int, slot: Slot) {
    + Click(windowID, slot, 0, ClickType.THROW)
}

fun InventoryTask.Builder.throwAll(slot: Slot) {
    throwAll(0, slot)
}

fun InventoryTask.Builder.throwAll(windowID: Int, slot: Slot) {
    + Click(windowID, slot, 1, ClickType.THROW)
}