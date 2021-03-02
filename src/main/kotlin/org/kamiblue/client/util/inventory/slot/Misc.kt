package org.kamiblue.client.util.inventory.slot

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import org.kamiblue.client.util.Wrapper

/**
 * Find an empty slot or slot that matches [predicate]
 * or slot 0 if none of those were found
 */
inline fun EntityPlayer.anyHotbarSlot(predicate: (ItemStack) -> Boolean): HotbarSlot {
    val hotbarSlots = this.hotbarSlots
    return hotbarSlots.firstEmpty()
        ?: hotbarSlots.firstByStack(predicate)
        ?: this.firstHotbarSlot
}

/**
 * Find an empty slot or slot 0
 */
fun EntityPlayer.anyHotbarSlot() =
    this.hotbarSlots.firstEmpty()
        ?: this.firstHotbarSlot

fun Slot.toHotbarSlotOrNull() =
    if (this.slotNumber in 36..44 && this.inventory == Wrapper.player?.inventory) HotbarSlot(this)
    else null