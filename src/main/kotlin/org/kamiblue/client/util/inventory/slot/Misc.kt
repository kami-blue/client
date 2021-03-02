package org.kamiblue.client.util.inventory.slot

import net.minecraft.init.Items
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.util.Wrapper

/**
 * Find an empty slot or slot that matches [predicate]
 * or slot 0 if none of those were found
 */
inline fun SafeClientEvent.findAnyHotbarSlot(predicate: (ItemStack) -> Boolean): HotbarSlot {
    val hotbarSlots = player.hotbarSlots
    return hotbarSlots.firstItem(Items.AIR)
        ?: hotbarSlots.firstByStack(predicate)
        ?: player.firstHotbarSlot
}

fun Slot.toHotbarSlotOrNull() =
    if (this.slotNumber in 36..44 && this.inventory == Wrapper.player?.inventory) HotbarSlot(this)
    else null