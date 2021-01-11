package me.zeroeightsix.kami.util

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.util.threads.onMainThreadSafe
import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.inventory.Container
import net.minecraft.inventory.Slot
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketClickWindow

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
 * Swaps the item in [slotFrom] with the first empty hotbar slot
 * or matches with [predicate] or slot 0 if none of those found
 */
fun SafeClientEvent.moveToHotbar(slotFrom: Slot, predicate: (ItemStack) -> Boolean): Short {
    return moveToHotbar(slotFrom.slotNumber, predicate)
}

/**
 * Swaps the item in [slotFrom] with the first empty hotbar slot
 * or matches with [predicate] or slot 0 if none of those found
 */
fun SafeClientEvent.moveToHotbar(slotFrom: Int, predicate: (ItemStack) -> Boolean): Short {
    val hotbarSlots = player.hotbarSlots
    val slotTo = hotbarSlots.firstItem(Items.AIR)?.hotbarSlot
        ?: hotbarSlots.firstByStack(predicate)?.hotbarSlot ?: 0

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
fun SafeClientEvent.moveToHotbar(windowId: Int, slotFrom: Slot, hotbarSlotTo: HotbarSlot): Short {
    return moveToHotbar(windowId, slotFrom.slotNumber, hotbarSlotTo.hotbarSlot)
}

/**
 * Swaps the item in [slotFrom] with the hotbar slot [hotbarSlotTo].
 */
fun SafeClientEvent.moveToHotbar(slotFrom: Int, hotbarSlotTo: Int): Short {
    return moveToHotbar(0, slotFrom, hotbarSlotTo)
}

/**
 * Swaps the item in [slotFrom] with the hotbar slot [hotbarSlotTo].
 */
fun SafeClientEvent.moveToHotbar(windowId: Int, slotFrom: Int, hotbarSlotTo: Int): Short {
    // mouseButton is actually the hotbar
    swapToSlot(hotbarSlotTo)
    return clickSlot(windowId, slotFrom, hotbarSlotTo, type = ClickType.SWAP)
}

/**
 * Move the item in [slotFrom]  to [slotTo] in player inventory,
 * if [slotTo] contains an item, then move it to [slotFrom]
 */
fun SafeClientEvent.moveToSlot(slotFrom: Slot, slotTo: Slot): ShortArray {
    return moveToSlot(0, slotFrom.slotNumber, slotTo.slotNumber)
}

/**
 * Move the item in [slotFrom]  to [slotTo] in player inventory,
 * if [slotTo] contains an item, then move it to [slotFrom]
 */
fun SafeClientEvent.moveToSlot(slotFrom: Int, slotTo: Int): ShortArray {
    return moveToSlot(0, slotFrom, slotTo)
}

/**
 * Move the item in [slotFrom] to [slotTo] in [windowId],
 * if [slotTo] contains an item, then move it to [slotFrom]
 */
fun SafeClientEvent.moveToSlot(windowId: Int, slotFrom: Int, slotTo: Int): ShortArray {
    return shortArrayOf(
        clickSlot(windowId, slotFrom, type = ClickType.PICKUP),
        clickSlot(windowId, slotTo, type = ClickType.PICKUP),
        clickSlot(windowId, slotFrom, type = ClickType.PICKUP)
    )
}

/**
 * Move all the item that equals to the item in [slotTo] to [slotTo] in player inventory
 * Note: Not working
 */
fun SafeClientEvent.moveAllToSlot(slotTo: Int): ShortArray {
    return shortArrayOf(
        clickSlot(slot = slotTo, type = ClickType.PICKUP_ALL),
        clickSlot(slot = slotTo, type = ClickType.PICKUP)
    )
}

/**
 * Quick move (Shift + Click) the item in [slot] in player inventory
 */
fun SafeClientEvent.quickMoveSlot(slot: Int): Short {
    return quickMoveSlot(0, slot)
}

/**
 * Quick move (Shift + Click) the item in [slot] in specified [windowId]
 */
fun SafeClientEvent.quickMoveSlot(windowId: Int, slot: Int): Short {
    return clickSlot(windowId, slot, type = ClickType.QUICK_MOVE)
}

/**
 * Quick move (Shift + Click) the item in [slot] in player inventory
 */
fun SafeClientEvent.quickMoveSlot(slot: Slot): Short {
    return quickMoveSlot(0, slot)
}

/**
 * Quick move (Shift + Click) the item in [slot] in specified [windowId]
 */
fun SafeClientEvent.quickMoveSlot(windowId: Int, slot: Slot): Short {
    return clickSlot(windowId, slot, type = ClickType.QUICK_MOVE)
}


/**
 * Throw all the item in [slot] in player inventory
 */
fun SafeClientEvent.throwAllInSlot(slot: Int): Short {
    return throwAllInSlot(0, slot)
}

/**
 * Throw all the item in [slot] in specified [windowId]
 */
fun SafeClientEvent.throwAllInSlot(windowId: Int, slot: Int): Short {
    return clickSlot(windowId, slot, 1, ClickType.THROW)
}

/**
 * Throw all the item in [slot] in player inventory
 */
fun SafeClientEvent.throwAllInSlot(slot: Slot): Short {
    return throwAllInSlot(0, slot)
}

/**
 * Throw all the item in [slot] in specified [windowId]
 */
fun SafeClientEvent.throwAllInSlot(windowId: Int, slot: Slot): Short {
    return clickSlot(windowId, slot, 1, ClickType.THROW)
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
fun SafeClientEvent.clickSlot(windowId: Int = 0, slot: Slot, mouseButton: Int = 0, type: ClickType): Short {
    return clickSlot(windowId, slot.slotNumber, mouseButton, type)
}

/**
 * Performs inventory clicking in specific window, slot, mouseButton, and click type
 *
 * @return Transaction id
 */
fun SafeClientEvent.clickSlot(windowId: Int = 0, slot: Int, mouseButton: Int = 0, type: ClickType): Short {
    val container = if (windowId == 0) player.inventoryContainer else player.openContainer
    container ?: return -32768

    val playerInventory = player.inventory ?: return -32768
    val transactionID = container.getNextTransactionID(playerInventory)
    val itemStack = container.slotClick(slot, mouseButton, type, player)

    connection.sendPacket(CPacketClickWindow(windowId, slot, mouseButton, type, itemStack, transactionID))
    onMainThreadSafe { playerController.updateController() }

    return transactionID
}

val EntityPlayer.allSlots: List<Slot>
    get() = inventoryContainer.getSlots(1..45)

val EntityPlayer.armorSlots: List<Slot>
    get() = inventoryContainer.getSlots(5..8)

val EntityPlayer.offhandSlot: Slot
    get() = inventoryContainer.inventorySlots[45]

val EntityPlayer.craftingSlots: List<Slot>
    get() = inventoryContainer.getSlots(1..4)

val EntityPlayer.inventorySlots: List<Slot>
    get() = inventoryContainer.getSlots(9..44)

val EntityPlayer.storageSlots: List<Slot>
    get() = inventoryContainer.getSlots(9..35)

val EntityPlayer.hotbarSlots: List<HotbarSlot>
    get() = ArrayList<HotbarSlot>().apply {
        for (slot in 36..44) {
            add(HotbarSlot(inventoryContainer.inventorySlots[slot]))
        }
    }

fun Container.getSlots(range: IntRange): List<Slot> =
    inventorySlots.subList(range.first, range.last + 1)


fun Iterable<Slot>.countEmpty() =
    count { it.stack.isEmpty }

inline fun <reified B : Block> Iterable<Slot>.countBlock(crossinline predicate: (ItemStack) -> Boolean = { true }) =
    countByStack { itemStack ->
        itemStack.item.let { it is ItemBlock && it.block is B } && predicate(itemStack)
    }

fun Iterable<Slot>.countBlock(block: Block, predicate: (ItemStack) -> Boolean = { true }) =
    countByStack { itemStack ->
        itemStack.item.let { it is ItemBlock && it.block == block } && predicate(itemStack)
    }

inline fun <reified I : Item> Iterable<Slot>.countItem(crossinline predicate: (ItemStack) -> Boolean = { true }) =
    countByStack { it.item is I && predicate(it) }

fun Iterable<Slot>.countItem(item: Item, predicate: (ItemStack) -> Boolean = { true }) =
    countByStack { it.item == item && predicate(it) }

fun Iterable<Slot>.countID(itemID: Int, predicate: (ItemStack) -> Boolean = { true }) =
    countByStack { it.item.id == itemID && predicate(it) }

fun Iterable<Slot>.countByStack(predicate: (ItemStack) -> Boolean = { true }) =
    sumBy { slot ->
        slot.stack.let { if (predicate(it)) it.count else 0 }
    }


fun <T : Slot> Iterable<T>.firstEmpty() =
    firstByStack { it.isEmpty }

inline fun <reified B : Block, T : Slot> Iterable<T>.firstBlock(crossinline predicate: (ItemStack) -> Boolean = { true }) =
    firstByStack { itemStack ->
        itemStack.item.let { it is ItemBlock && it.block is B } && predicate(itemStack)
    }

fun <T : Slot> Iterable<T>.firstBlock(block: Block, predicate: (ItemStack) -> Boolean = { true }) =
    firstByStack { itemStack ->
        itemStack.item.let { it is ItemBlock && it.block == block } && predicate(itemStack)
    }

inline fun <reified I : Item, T : Slot> Iterable<T>.firstItem(crossinline predicate: (ItemStack) -> Boolean = { true }) =
    firstByStack {
        it.item is I && predicate(it)
    }

fun <T : Slot> Iterable<T>.firstItem(item: Item, predicate: (ItemStack) -> Boolean = { true }) =
    firstByStack {
        it.item == item && predicate(it)
    }

fun <T : Slot> Iterable<T>.firstID(itemID: Int, predicate: (ItemStack) -> Boolean = { true }) =
    firstByStack {
        it.item.id == itemID && predicate(it)
    }

fun <T : Slot> Iterable<T>.firstByStack(predicate: (ItemStack) -> Boolean): T? =
    firstOrNull { predicate(it.stack) }


inline fun <reified B : Block, T : Slot> Iterable<T>.forBlock(predicate: (ItemStack) -> Boolean = { true }) =
    filter { slot ->
        slot.stack.let { itemStack ->
            itemStack.item.let { it is ItemBlock && it.block is B } && predicate(itemStack)
        }
    }

fun <T : Slot> Iterable<T>.forBlock(block: Block, predicate: (ItemStack) -> Boolean = { true }) =
    filter { slot ->
        slot.stack.let { itemStack ->
            itemStack.item.let { it is ItemBlock && it.block == block } && predicate(itemStack)
        }
    }

inline fun <reified I : Item, T : Slot> Iterable<T>.forItem(predicate: (ItemStack) -> Boolean = { true }) =
    filter { slot ->
        slot.stack.let { it.item is I && predicate(it) }
    }

fun <T : Slot> Iterable<T>.forItem(item: Item, predicate: (ItemStack) -> Boolean = { true }) =
    filter { slot ->
        slot.stack.let { it.item == item && predicate(it) }
    }

fun <T : Slot> Iterable<T>.forID(itemID: Int, predicate: (ItemStack) -> Boolean = { true }) =
    filter { slot ->
        slot.stack.let { it.item.id == itemID && predicate(it) }
    }


class HotbarSlot(slot: Slot) : Slot(slot.inventory, slot.slotIndex, slot.xPos, slot.yPos) {
    init {
        slotNumber = slot.slotNumber
    }

    val hotbarSlot = slot.slotNumber - 36
}