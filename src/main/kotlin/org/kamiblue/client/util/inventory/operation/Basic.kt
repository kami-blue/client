package org.kamiblue.client.util.inventory.operation

import net.minecraft.inventory.ClickType
import net.minecraft.inventory.Slot
import org.kamiblue.client.util.inventory.ClickInfo
import org.kamiblue.client.util.inventory.ClickTask
import org.kamiblue.client.util.inventory.slot.HotbarSlot

fun ClickTask.Builder.click(slot: Slot) {
    click(0, slot)
}

fun ClickTask.Builder.click(windowID: Int, slot: Slot) {
    + ClickInfo(windowID, slot, 0, ClickType.PICKUP)
}

fun ClickTask.Builder.shiftClick(slot: Slot) {
    shiftClick(0, slot)
}

fun ClickTask.Builder.shiftClick(windowID: Int, slot: Slot) {
    + ClickInfo(windowID, slot, 0, ClickType.QUICK_MOVE)
}

fun ClickTask.Builder.swapWith(slot: Slot, hotbarSlot: HotbarSlot) {
    swapWith(0, slot, hotbarSlot)
}

fun ClickTask.Builder.swapWith(windowID: Int, slot: Slot, hotbarSlot: HotbarSlot) {
    + ClickInfo(windowID, slot, hotbarSlot.hotbarSlot, ClickType.SWAP)
}