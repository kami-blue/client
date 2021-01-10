package me.zeroeightsix.kami.util

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.mixin.extension.syncCurrentPlayItem
import me.zeroeightsix.kami.util.threads.onMainThreadSafe
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.inventory.Container
import net.minecraft.inventory.Slot
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketClickWindow

object InventoryUtils {
    private val mc = Minecraft.getMinecraft()

    /**
     * Returns slots contains item with given item id in player hotbar
     *
     * @return Array contains slot index, null if no item found
     */
    fun getSlotsHotbar(itemId: Int): Array<Int>? {
        return getSlots(0, 8, itemId)
    }

    /**
     * Returns slots contains with given item id in player inventory (without hotbar)
     *
     * @return Array contains slot index, null if no item found
     */
    fun getSlotsNoHotbar(itemId: Int): Array<Int>? {
        return getSlots(9, 35, itemId)
    }

    /**
     * Returns slots contains item with given item id in player inventory
     *
     * @return Array contains slot index, null if no item found
     */
    fun getSlots(min: Int, max: Int, itemID: Int): Array<Int>? {
        val slots = ArrayList<Int>()
        mc.player?.inventory?.mainInventory?.let {
            val clonedList = ArrayList(it)
            for (i in min..max) {
                if (clonedList[i].item.id != itemID) continue
                slots.add(i)
            }
        }
        return if (slots.isNotEmpty()) slots.toTypedArray() else null
    }

    fun getEmptySlotContainer(min: Int, max: Int): Int? {
        return getSlotsContainer(min, max, 0)?.get(0)
    }

    fun getEmptySlotFullInv(min: Int, max: Int): Int? {
        return getSlotsFullInv(min, max, 0)?.get(0)
    }

    /**
     * Returns slots in full inventory contains item with given [itemId] in current open container
     *
     * @return Array contains full inventory slot index, null if no item found
     */
    fun getSlotsContainer(min: Int, max: Int, itemId: Int): Array<Int>? {
        val slots = ArrayList<Int>()
        mc.player?.openContainer?.inventory?.let {
            val clonedList = ArrayList(it)
            for (i in min..max) {
                if (clonedList[i].item.id != itemId) continue
                slots.add(i)
            }
        }
        return if (slots.isNotEmpty()) slots.toTypedArray() else null
    }

    /**
     * Returns slots contains item with given [itemId] in player hotbar
     * This is same as [getSlots] but it returns full inventory slot index
     *
     * @return Array contains slot index, null if no item found
     */
    fun getSlotsFullInvHotbar(itemId: Int): Array<Int>? {
        return getSlotsFullInv(36, 44, itemId)
    }

    /**
     * Returns slots contains with given [itemId] in player inventory (without hotbar)
     * This is same as [getSlots] but it returns full inventory slot index
     *
     * @return Array contains slot index, null if no item found
     */
    fun getSlotsFullInvNoHotbar(itemId: Int): Array<Int>? {
        return getSlotsFullInv(9, 35, itemId)
    }

    /**
     * Returns slots in full inventory contains item with given [itemId] in player inventory
     * This is same as [getSlots] but it returns full inventory slot index
     *
     * @return Array contains full inventory slot index, null if no item found
     */
    fun getSlotsFullInv(min: Int = 9, max: Int = 44, itemId: Int): Array<Int>? {
        val slots = ArrayList<Int>()
        mc.player?.inventoryContainer?.inventory?.let {
            val clonedList = ArrayList(it)
            for (i in min..max) {
                if (clonedList[i].item.id != itemId) continue
                slots.add(i)
            }
        }
        return if (slots.isNotEmpty()) slots.toTypedArray() else null
    }

    /**
     * Returns slots in full inventory contains [item] in player inventory
     * This is same as [getSlots] but it returns full inventory slot index
     *
     * @return Array contains full inventory slot index, null if no item found
     */
    fun getSlotsFullInv(min: Int = 9, max: Int = 44, item: Item): Array<Int>? {
        val slots = ArrayList<Int>()
        mc.player?.inventoryContainer?.inventory?.let {
            val clonedList = ArrayList(it)
            for (i in min..max) {
                if (clonedList[i].item != item) continue
                slots.add(i)
            }
        }
        return if (slots.isNotEmpty()) slots.toTypedArray() else null
    }

    /**
     * Counts number of item in hotbar
     *
     * @return Number of item with given [itemId] in hotbar
     */
    fun countItemHotbar(itemId: Int): Int {
        return countItem(36, 44, itemId)
    }

    /**
     * Counts number of item in non hotbar
     *
     * @return Number of item with given [itemId] in non hotbar
     */
    fun countItemNoHotbar(itemId: Int): Int {
        return countItem(0, 35, itemId)
    }

    /**
     * Counts number of item in inventory
     *
     * @return Number of item with given [itemId] in inventory
     */
    fun countItemAll(itemId: Int): Int {
        return countItem(0, 45, itemId)
    }

    /**
     * Counts number of item in inventory
     *
     * @return Number of [item] in inventory
     */
    fun countItemAll(item: Item): Int {
        return countItem(0, 45, item)
    }

    /**
     * Counts number of item in range of slots
     *
     * @return Number of item with given [itemId] from slot [min] to slot [max]
     */
    fun countItem(min: Int, max: Int, itemId: Int): Int {
        val itemList = getSlotsFullInv(min, max, itemId)
        var currentCount = 0
        if (itemList != null) {
            mc.player?.inventoryContainer?.inventory?.let {
                val clonedList = ArrayList(it)
                for (i in min..max) {
                    val itemStack = clonedList.getOrNull(i) ?: continue
                    if (itemStack.item.id != itemId) continue
                    currentCount += if (itemId == 0) 1 else itemStack.count
                }
            }
        }
        return currentCount
    }

    /**
     * Counts number of item in range of slots
     *
     * @return Number of [item] from slot [min] to slot [max]
     */
    fun countItem(min: Int, max: Int, item: Item): Int {
        var currentCount = 0
        mc.player?.inventoryContainer?.inventory?.let {
            val clonedList = ArrayList(it)
            for (i in min..max) {
                val itemStack = clonedList.getOrNull(i) ?: continue
                if (itemStack.item != item) continue
                currentCount += if (item == Items.AIR) 1 else itemStack.count
            }
        }
        return currentCount
    }

    /* Inventory management */
    /**
     * Swap current held item to given [slot]
     */
    fun swapSlot(slot: Int) {
        mc.player?.inventory?.currentItem = slot
        mc.playerController?.syncCurrentPlayItem()
    }

    /**
     * Try to swap current held item to item with given [itemID]
     */
    fun swapSlotToItem(itemID: Int) {
        val slot = getSlotsHotbar(itemID)?.getOrNull(0) ?: return
        swapSlot(slot)
    }

    /**
     * Try to move item with given [itemID] to empty hotbar slot or slot contains no exception [exceptionID]
     * If none of those found, then move it to slot 0
     * @return the inventory slot [itemID] was moved to, -1 if failed
     */
    fun moveToHotbar(itemID: Int, vararg exceptionID: Int): Int {
        val slotFrom = getSlotsFullInvNoHotbar(itemID)?.getOrNull(0) ?: return -1
        var slotTo = 36

        mc.player?.inventoryContainer?.inventory?.let {
            val clonedList = ArrayList(it)
            for (i in 36..44) { /* Finds slot contains no exception item first */
                val itemStack = clonedList[i]
                if (!exceptionID.contains(itemStack.item.id)) {
                    slotTo = i
                    break
                }
            }
        }

        moveToHotbarSlot(slotFrom, slotTo - 36)
        return slotTo
    }

    /**
     * Swaps the item in [slotFrom] with the item in the hotbar slot (0..9) [slotTo] in player inventory.
     */
    fun moveToHotbarSlot(slotFrom: Int, slotTo: Int): ShortArray {
        return moveToHotbarSlot(0, slotFrom, slotTo)
    }

    /**
     * Swaps the item in [slotFrom] with the item in the hotbar slot (0..9) [slotTo] in player inventory.
     */
    fun moveToHotbarSlot(windowId: Int, slotFrom: Int, slotTo: Int): ShortArray {
        return shortArrayOf(
            /* CPacketClickWindow spaghetti code moment: mouseButton is actually the hotbar
               slot (0..9) for ClickType.SWAP
             */
            inventoryClick(windowId, slotFrom, slotTo, type = ClickType.SWAP)
        )
    }

    /**
     * Move the item in [slotFrom]  to [slotTo] in player inventory,
     * if [slotTo] contains an item, then move it to [slotFrom]
     */
    fun moveToSlot(slotFrom: Int, slotTo: Int): ShortArray {
        return moveToSlot(0, slotFrom, slotTo)
    }

    /**
     * Move the item in [slotFrom] to [slotTo] in [windowId],
     * if [slotTo] contains an item, then move it to [slotFrom]
     */
    fun moveToSlot(windowId: Int, slotFrom: Int, slotTo: Int): ShortArray {
        return shortArrayOf(
            inventoryClick(windowId, slotFrom, type = ClickType.PICKUP),
            inventoryClick(windowId, slotTo, type = ClickType.PICKUP),
            inventoryClick(windowId, slotFrom, type = ClickType.PICKUP)
        )
    }

    /**
     * Move all the item that equals to the item in [slotTo] to [slotTo] in player inventory
     * Note: Not working
     */
    fun moveAllToSlot(slotTo: Int) {
        inventoryClick(slot = slotTo, type = ClickType.PICKUP_ALL)
        inventoryClick(slot = slotTo, type = ClickType.PICKUP)
    }

    /**
     * Quick move (Shift + Click) the item in [slotFrom] in player inventory
     */
    fun quickMoveSlot(slotFrom: Int): Short {
        return quickMoveSlot(0, slotFrom)
    }

    /**
     * Quick move (Shift + Click) the item in [slotFrom] in specified [windowId]
     */
    fun quickMoveSlot(windowId: Int, slotFrom: Int): Short {
        return inventoryClick(windowId, slotFrom, type = ClickType.QUICK_MOVE)
    }

    /**
     * Throw all the item in [slot] in player inventory
     */
    fun throwAllInSlot(slot: Int) {
        throwAllInSlot(0, slot)
    }

    /**
     * Throw all the item in [slot] in specified [windowId]
     */
    fun throwAllInSlot(windowId: Int, slot: Int) {
        inventoryClick(windowId, slot, 1, ClickType.THROW)
    }

    /**
     * Put the item currently holding by mouse to somewhere or throw it
     */
    fun removeHoldingItem() {
        if (mc.player?.inventory?.itemStack?.isEmpty != false) return
        val slot = (getSlotsFullInv(9, 45, 0) // Get empty slots in inventory and offhand
            ?: getSlotsFullInv(1, 4, 0))?.get(0) // Get empty slots in crafting slot
            ?: -999 // Throw on the ground
        inventoryClick(slot = slot, type = ClickType.PICKUP)
    }

    /**
     * Performs inventory clicking in specific window, slot, mouseButton, add click type
     *
     * @return Transaction id
     */
    fun inventoryClick(windowId: Int = 0, slot: Int, mouseButton: Int = 0, type: ClickType): Short {
        val player = mc.player ?: return -32768
        val container = (if (windowId == 0) player.inventoryContainer else player.openContainer) ?: return -32768
        val playerInventory = player.inventory ?: return -32768
        val transactionID = container.getNextTransactionID(playerInventory)
        val itemStack = container.slotClick(slot, mouseButton, type, player)
        mc.connection?.sendPacket(CPacketClickWindow(windowId, slot, mouseButton, type, itemStack, transactionID))
        return transactionID
    }
    /* End of inventory management */
}

/**
 * Try to swap selected hotbar slot to [I] that matches with [predicate]
 */
inline fun <reified I : Item> SafeClientEvent.swapToItem(predicate: (ItemStack) -> Boolean = { true }): Boolean {
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
 * Try to swap selected hotbar slot to item with [itemID]
 * and matches with [predicate]
 */
fun SafeClientEvent.swapToItem(itemID: Int, predicate: (ItemStack) -> Boolean = { true }): Boolean {
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
    player.inventory.currentItem = slot
    playerController.updateController()
}

/**
 * Swaps the item in [slotFrom] with the item in the hotbar slot (0..9) [slotTo] in player inventory.
 */
fun SafeClientEvent.moveToHotbar(slotFrom: Slot, slotTo: HotbarSlot): Short {
    return moveToHotbar(0, slotFrom, slotTo)
}

/**
 * Swaps the item in [slotFrom] with the item in the hotbar slot (0..9) [slotTo] in player inventory.
 */
fun SafeClientEvent.moveToHotbar(windowId: Int, slotFrom: Slot, slotTo: HotbarSlot): Short {
    return moveToHotbar(windowId, slotFrom.slotNumber, slotTo.hotbarSlot)
}

/**
 * Swaps the item in [slotFrom] with the item in the hotbar slot (0..8) [slotTo] in player inventory.
 */
fun SafeClientEvent.moveToHotbar(slotFrom: Int, slotTo: Int): Short {
    return moveToHotbar(0, slotFrom, slotTo)
}

/**
 * Swaps the item in [slotFrom] with the item in the hotbar slot (0..8) [slotTo] in player inventory.
 */
fun SafeClientEvent.moveToHotbar(windowId: Int, slotFrom: Int, slotTo: Int): Short {
    // mouseButton is actually the hotbar
    return clickSlot(windowId, slotFrom, slotTo, type = ClickType.SWAP)
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


val EntityPlayer.armorSlots: List<Slot>
    get() = inventoryContainer.getSlots(5..8)

val EntityPlayer.offhandSlot: Slot
    get() = inventoryContainer.inventorySlots[45]

val EntityPlayer.craftingSlots: List<Slot>
    get() = inventoryContainer.getSlots(1..4)

val EntityPlayer.inventorySlots: List<Slot>
    get() = inventoryContainer.getSlots(0..44)

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


inline fun <reified B : Block, T : Slot> Iterable<T>.countBlock(crossinline predicate: (ItemStack) -> Boolean = { true }) =
    count { itemStack ->
        itemStack.item.let { it is ItemBlock && it.block is B } && predicate(itemStack)
    }

fun <T : Slot> Iterable<T>.countBlock(block: Block, predicate: (ItemStack) -> Boolean = { true }) =
    count { itemStack ->
        itemStack.item.let { it is ItemBlock && it.block == block } && predicate(itemStack)
    }

inline fun <reified I : Item, T : Slot> Iterable<T>.countItem(crossinline predicate: (ItemStack) -> Boolean = { true }) =
    count { it.item is I && predicate(it) }

fun <T : Slot> Iterable<T>.countItem(item: Item, predicate: (ItemStack) -> Boolean = { true }) =
    count { it.item == item && predicate(it) }

fun <T : Slot> Iterable<T>.countID(itemID: Int, predicate: (ItemStack) -> Boolean = { true }) =
    count { it.item.id == itemID && predicate(it) }


fun <T : Slot> Iterable<T>.count(predicate: (ItemStack) -> Boolean = { true }) =
    sumBy { slot ->
        slot.stack.let { if (predicate(it)) it.count else 0 }
    }


inline fun <reified B : Block, T : Slot> Iterable<T>.firstBlock(predicate: (ItemStack) -> Boolean = { true }) =
    firstOrNull { slot ->
        slot.stack.let { itemStack ->
            itemStack.item.let { it is ItemBlock && it.block is B } && predicate(itemStack)
        }
    }

fun <T : Slot> Iterable<T>.firstBlock(block: Block, predicate: (ItemStack) -> Boolean = { true }) =
    firstOrNull { slot ->
        slot.stack.let { itemStack ->
            itemStack.item.let { it is ItemBlock && it.block == block } && predicate(itemStack)
        }
    }

inline fun <reified I : Item, T : Slot> Iterable<T>.firstItem(predicate: (ItemStack) -> Boolean = { true }) =
    firstOrNull { slot ->
        slot.stack.let { it.item is I && predicate(it) }
    }

fun <T : Slot> Iterable<T>.firstItem(item: Item, predicate: (ItemStack) -> Boolean = { true }) =
    firstOrNull { slot ->
        slot.stack.let { it.item == item && predicate(it) }
    }

fun <T : Slot> Iterable<T>.firstID(itemID: Int, predicate: (ItemStack) -> Boolean = { true }) =
    firstOrNull { slot ->
        slot.stack.let { it.item.id == itemID && predicate(it) }
    }


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