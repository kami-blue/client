package org.kamiblue.client.util.items

import net.minecraft.block.Block
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.inventory.Slot
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.util.inventory.slot.*

/**
 * Try to swap selected hotbar slot to [I] that matches with [predicate]
 */
inline fun <reified I : Block> SafeClientEvent.swapToBlock(crossinline predicate: (ItemStack) -> Boolean = { true }): Boolean {
    return player.hotbarSlots.firstBlock<I, HotbarSlot>(predicate)?.let {
        swapToSlot(it)
        true
    } ?: false
}

/**
 * Try to swap selected hotbar slot to [block] that matches with [predicate]
 */
fun SafeClientEvent.swapToBlock(block: Block, predicate: (ItemStack) -> Boolean = { true }): Boolean {
    return player.hotbarSlots.firstBlock(block, predicate)?.let {
        swapToSlot(it)
        true
    } ?: false
}

/**
 * Try to swap selected hotbar slot to [I] that matches with [predicate]
 */
inline fun <reified I : Item> SafeClientEvent.swapToItem(crossinline predicate: (ItemStack) -> Boolean = { true }): Boolean {
    return player.hotbarSlots.firstItem<I, HotbarSlot>(predicate)?.let {
        swapToSlot(it)
        true
    } ?: false
}

/**
 * Try to swap selected hotbar slot to [item] that matches with [predicate]
 */
fun SafeClientEvent.swapToItem(item: Item, predicate: (ItemStack) -> Boolean = { true }): Boolean {
    return player.hotbarSlots.firstItem(item, predicate)?.let {
        swapToSlot(it)
        true
    } ?: false
}

/**
 * Try to swap selected hotbar slot to item with [itemID] that matches with [predicate]
 */
fun SafeClientEvent.swapToID(itemID: Int, predicate: (ItemStack) -> Boolean = { true }): Boolean {
    return player.hotbarSlots.firstID(itemID, predicate)?.let {
        swapToSlot(it)
        true
    } ?: false
}

/**
 * Swap the selected hotbar slot to [hotbarSlot]
 */
fun SafeClientEvent.swapToSlot(hotbarSlot: HotbarSlot) {
    swapToSlot(hotbarSlot.hotbarSlot)
}

/**
 * Swap the selected hotbar slot to [slot]
 */
fun SafeClientEvent.swapToSlot(slot: Int) {
    if (slot !in 0..8) return
    player.inventory.currentItem = slot
    playerController.updateController()
}


/**
 * Try to swap selected hotbar slot to [I] that matches with [predicateItem]
 *
 * Or move an item from storage slot to an empty slot or slot that matches [predicateSlot]
 * or slot 0 if none
 */
inline fun <reified I : Block> SafeClientEvent.swapToBlockOrMove(
    crossinline predicateItem: (ItemStack) -> Boolean = { true },
    noinline predicateSlot: (ItemStack) -> Boolean = { true }
): Boolean {
    return if (swapToBlock<I>(predicateItem)) {
        true
    } else {
        player.storageSlots.firstBlock<I, Slot>(predicateItem)?.let {
            moveToHotbar(it, predicateSlot)
            true
        } ?: false
    }
}

/**
 * Try to swap selected hotbar slot to [block] that matches with [predicateItem]
 *
 * Or move an item from storage slot to an empty slot or slot that matches [predicateSlot]
 * or slot 0 if none
 */
fun SafeClientEvent.swapToBlockOrMove(
    block: Block,
    predicateItem: (ItemStack) -> Boolean = { true },
    predicateSlot: (ItemStack) -> Boolean = { true }
): Boolean {
    return if (swapToBlock(block, predicateItem)) {
        true
    } else {
        player.storageSlots.firstBlock(block, predicateItem)?.let {
            moveToHotbar(it, predicateSlot)
            true
        } ?: false
    }
}

/**
 * Try to swap selected hotbar slot to [I] that matches with [predicateItem]
 *
 * Or move an item from storage slot to an empty slot or slot that matches [predicateSlot]
 * or slot 0 if none
 */
inline fun <reified I : Item> SafeClientEvent.swapToItemOrMove(
    crossinline predicateItem: (ItemStack) -> Boolean = { true },
    noinline predicateSlot: (ItemStack) -> Boolean = { true }
): Boolean {
    return if (swapToItem<I>(predicateItem)) {
        true
    } else {
        player.storageSlots.firstItem<I, Slot>(predicateItem)?.let {
            moveToHotbar(it, predicateSlot)
            true
        } ?: false
    }
}

/**
 * Try to swap selected hotbar slot to [item] that matches with [predicateItem]
 *
 * Or move an item from storage slot to an empty slot or slot that matches [predicateSlot]
 * or slot 0 if none
 */
fun SafeClientEvent.swapToItemOrMove(
    item: Item,
    predicateItem: (ItemStack) -> Boolean = { true },
    predicateSlot: (ItemStack) -> Boolean = { true }
): Boolean {
    return if (swapToItem(item, predicateItem)) {
        true
    } else {
        player.storageSlots.firstItem(item, predicateItem)?.let {
            moveToHotbar(it, predicateSlot)
            true
        } ?: false
    }
}

/**
 * Try to swap selected hotbar slot to item with [itemID] that matches with [predicateItem]
 *
 * Or move an item from storage slot to an empty slot or slot that matches [predicateSlot]
 * or slot 0 if none
 */
fun SafeClientEvent.swapToItemOrMove(
    itemID: Int,
    predicateItem: (ItemStack) -> Boolean = { true },
    predicateSlot: (ItemStack) -> Boolean = { true }
): Boolean {
    return if (swapToID(itemID, predicateItem)) {
        true
    } else {
        player.storageSlots.firstID(itemID, predicateItem)?.let {
            moveToHotbar(it, predicateSlot)
            true
        } ?: false
    }
}

/**
 * Swaps the item in [slotFrom] with the first empty hotbar slot
 * or matches with [predicate] or slot 0 if none of those found
 */
fun SafeClientEvent.moveToHotbar(slotFrom: Slot, predicate: (ItemStack) -> Boolean): Short {
    val hotbarSlots = player.hotbarSlots
    val slotTo = hotbarSlots.firstItem(Items.AIR)
        ?: hotbarSlots.firstByStack(predicate)
        ?: player.firstHotbarSlot

    return moveToHotbar(slotFrom, slotTo)
}

/**
 * Swaps the item in [slotFrom] with the hotbar slot [slotTo].
 */
fun SafeClientEvent.moveToHotbar(slotFrom: Slot, slotTo: HotbarSlot): Short {
    return moveToHotbar(0, slotFrom, slotTo)
}

/**
 * Swaps the item in [slotFrom] with the hotbar slot [hotbarSlotTo].
 */
fun SafeClientEvent.moveToHotbar(windowID: Int, slotFrom: Slot, hotbarSlotTo: HotbarSlot): Short {
    // mouseButton is actually the hotbar
    return clickSlot(windowID, slotFrom, hotbarSlotTo.hotbarSlot, type = ClickType.SWAP)
}