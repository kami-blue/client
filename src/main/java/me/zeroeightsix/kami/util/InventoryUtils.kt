package me.zeroeightsix.kami.util

import net.minecraft.client.Minecraft
import net.minecraft.inventory.ClickType
import net.minecraft.item.Item.getIdFromItem
import net.minecraft.network.play.client.CPacketClickWindow

object InventoryUtils {
    private val mc = Minecraft.getMinecraft()

    /**
     * Returns slots contains item with given item id in player inventory
     *
     * @return Array contains slot index, null if no item found
     */
    fun getSlots(min: Int, max: Int, itemID: Int): Array<Int>? {
        val slots = arrayListOf<Int>()
        for (i in min..max) {
            if (getIdFromItem(mc.player.inventory.getStackInSlot(i).getItem()) == itemID) {
                slots.add(i)
            }
        }
        return if (slots.isNotEmpty()) slots.toTypedArray() else null
    }

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
        val slots = arrayListOf<Int>()
        for (i in min..max) {
            if (getIdFromItem(mc.player.openContainer.inventory[i].getItem()) == itemId) {
                slots.add(i)
            }
        }
        return if (slots.isNotEmpty()) slots.toTypedArray() else null
    }

    /**
     * Returns slots in full inventory contains item with given [itemId] in player inventory
     * This is same as [getSlots] but it returns full inventory slot index
     *
     * @return Array contains full inventory slot index, null if no item found
     */
    fun getSlotsFullInv(min: Int, max: Int, itemId: Int): Array<Int>? {
        val slots = arrayListOf<Int>()
        for (i in min..max) {
            if (getIdFromItem(mc.player.inventoryContainer.inventory[i].getItem()) == itemId) {
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
     * Counts number of item in hotbar
     *
     * @return Number of item with given [itemId] in hotbar
     */
    fun countItemHotbar(itemId: Int): Int {
        val itemList = getSlots(0, 8, itemId)
        var currentCount = 0
        if (itemList != null) {
            for (i in itemList) {
                currentCount += mc.player.inventory.getStackInSlot(i).count
            }
        }
        return currentCount
    }

    /**
     * Counts number of item in non hotbar
     *
     * @return Number of item with given [itemId] in non hotbar
     */
    fun countItemNoHotbar(itemId: Int): Int {
        val itemList = getSlots(9, 35, itemId)
        var currentCount = 0
        if (itemList != null) {
            for (i in itemList) {
                currentCount += mc.player.inventory.getStackInSlot(i).count
            }
        }
        return currentCount
    }

    /**
     * Counts number of item in inventory
     *
     * @return Number of item with given [itemId] in inventory
     */
    @JvmStatic
    fun countItemAll(itemId: Int): Int {
        val itemList = getSlots(0, 35, itemId)
        var currentCount = 0
        if (itemList != null) {
            for (i in itemList) {
                currentCount += mc.player.inventory.getStackInSlot(i).count
            }
        }
        return currentCount
    }

    /**
     * Counts number of item in range of slots
     *
     * @return Number of item with given [itemId] from slot [min] to slot [max]
     */
    fun countItem(min: Int, max: Int, itemId: Int): Int {
        val itemList = getSlots(min, max, itemId)
        var currentCount = 0
        if (itemList != null) {
            for (i in itemList) {
                currentCount += mc.player.inventory.getStackInSlot(i).count
            }
        }
        return currentCount
    }

    /* Inventory management */
    /**
     * Swap current held item to given [slot]
     */
    fun swapSlot(slot: Int) {
        mc.player.inventory.currentItem = slot
        mc.playerController.syncCurrentPlayItem()
    }

    /**
     * Try to swap current held item to item with given [itemID]
     */
    fun swapSlotToItem(itemID: Int) {
        if (getSlotsHotbar(itemID) != null) {
            swapSlot(getSlotsHotbar(itemID)!![0])
        }
        mc.playerController.syncCurrentPlayItem()
    }

    /**
     * Try to move item with given [itemID] to empty hotbar slot or slot contains no exception [exceptionID]
     * If none of those found, then move it to slot 0
     */
    fun moveToHotbar(itemID: Int, exceptionID: Int) {
        val slot1 = getSlotsFullInvNoHotbar(itemID)!![0]
        var slot2 = 36
        for (i in 36..44) { /* Finds slot contains no exception item first */
            val currentItemStack = mc.player.inventoryContainer.inventory[i]
            if (currentItemStack.isEmpty) {
                slot2 = i
                break
            }
            if (getIdFromItem(currentItemStack.getItem()) != exceptionID) {
                slot2 = i
                break
            }
        }
        moveToSlot(slot1, slot2)
    }

    /**
     * Move the item in [slotFrom]  to [slotTo] in player inventory,
     * if [slotTo] contains an item, then move it to [slotFrom]
     */
    fun moveToSlot(slotFrom: Int, slotTo: Int) {
        moveToSlot(0, slotFrom, slotTo)
    }

    /**
     * Move the item in [slotFrom] to [slotTo] in [windowId],
     * if [slotTo] contains an item, then move it to [slotFrom]
     */
    fun moveToSlot(windowId: Int, slotFrom: Int, slotTo: Int) {
        inventoryClick(windowId, slotFrom, type = ClickType.PICKUP)
        inventoryClick(windowId, slotTo, type = ClickType.PICKUP)
        inventoryClick(windowId, slotFrom, type = ClickType.PICKUP)
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
    fun quickMoveSlot(slotFrom: Int) {
        quickMoveSlot(0, slotFrom)
    }

    /**
     * Quick move (Shift + Click) the item in [slotFrom] in specified [windowId]
     */
    fun quickMoveSlot(windowId: Int, slotFrom: Int) {
        inventoryClick(windowId, slotFrom, type = ClickType.QUICK_MOVE)
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
        if (mc.player.inventory.getItemStack().isEmpty()) return
        val slot = (getSlotsFullInv(9, 45, 0) // Get empty slots in inventory and offhand
                ?: getSlotsFullInv(1, 4, 0))?.get(0) // Get empty slots in crafting slot
                ?: -999 // Throw on the ground
        inventoryClick(slot = slot, type = ClickType.PICKUP)
    }

    private fun inventoryClick(windowId: Int = 0, slot: Int, mousedButton: Int = 0, type: ClickType) {
        val container = if (windowId == 0) mc.player.inventoryContainer else mc.player.openContainer
        val transactionID = container.getNextTransactionID(mc.player.inventory)
        val itemStack = container.slotClick(slot, mousedButton, type, mc.player)
        mc.connection!!.sendPacket(CPacketClickWindow(windowId, slot, mousedButton, type, itemStack, transactionID))
    }
    /* End of inventory management */
}