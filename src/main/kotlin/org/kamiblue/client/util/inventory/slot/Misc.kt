package org.kamiblue.client.util.inventory.slot

import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import org.kamiblue.client.event.SafeClientEvent

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