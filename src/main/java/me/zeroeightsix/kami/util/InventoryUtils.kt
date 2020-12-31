package me.zeroeightsix.kami.util

import me.zeroeightsix.kami.mixin.extension.syncCurrentPlayItem
import net.minecraft.client.Minecraft
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.item.Item
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
    @JvmStatic
    fun countItemAll(itemId: Int): Int {
        return countItem(0, 45, itemId)
    }

    /**
     * Counts number of item in inventory
     *
     * @return Number of [item] in inventory
     */
    @JvmStatic
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

        moveToSlot(slotFrom, slotTo)
        return slotTo
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
     *
     * @return Transaction id
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