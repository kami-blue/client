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

fun InventoryTask.Builder.click(slot: Slot) {
    click(0, slot)
}

fun InventoryTask.Builder.click(windowID: Int, slot: Slot) {
    + Click(windowID, slot, 0, ClickType.PICKUP)
}

fun InventoryTask.Builder.shiftClick(slot: Slot) {
    shiftClick(0, slot)
}

fun InventoryTask.Builder.shiftClick(windowID: Int, slot: Slot) {
    + Click(windowID, slot, 0, ClickType.QUICK_MOVE)
}

fun InventoryTask.Builder.swapWith(slot: Slot, hotbarSlot: HotbarSlot) {
    swapWith(0, slot, hotbarSlot)
}

fun InventoryTask.Builder.swapWith(windowID: Int, slot: Slot, hotbarSlot: HotbarSlot) {
    + Click(windowID, slot, hotbarSlot.hotbarSlot, ClickType.SWAP)
}