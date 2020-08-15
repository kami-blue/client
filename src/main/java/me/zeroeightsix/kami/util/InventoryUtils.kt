package me.zeroeightsix.kami.util

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.inventory.ClickType
import net.minecraft.item.Item.getIdFromItem

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
    var inProgress = false

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

    private fun inventoryClick(slot: Int, type: ClickType) {
        inventoryClick(mc.player.inventoryContainer.windowId, slot, type)
    }

    private fun inventoryClick(windowID: Int, slot: Int, type: ClickType) {
        mc.playerController.windowClick(windowID, slot, 0, type, mc.player)
    }

    /**
     * Try to move item with given [itemID] to empty hotbar slot or slot contains no exception [exceptionID]
     * If none of those found, then move it to slot 0
     */
    fun moveToHotbar(itemID: Int, exceptionID: Int, delayMillis: Long) {
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
        moveToSlot(slot1, slot2, delayMillis)
    }

    /**
     * Move the item in [slotFrom] in player inventory to [slotTo] in player inventory , if [slotTo] contains an item,
     * then move it to [slotFrom]
     */
    fun moveToSlot(slotFrom: Int, slotTo: Int, delayMillis: Long) {
        moveToSlot(mc.player.inventoryContainer.windowId, slotFrom, slotTo, delayMillis)
    }

    /**
     * Move the item in [slotFrom] in [windowId] to [slotTo] in [windowId],
     * if [slotTo] contains an item, then move it to [slotFrom]
     */
    fun moveToSlot(windowId: Int, slotFrom: Int, slotTo: Int, delayMillis: Long) {
        if (inProgress) return
        Thread(Runnable {
            inProgress = true
            val prevScreen = mc.currentScreen
            if (prevScreen !is GuiContainer) mc.displayGuiScreen(GuiInventory(mc.player))
            Thread.sleep(delayMillis)
            inventoryClick(windowId, slotFrom, ClickType.PICKUP)
            Thread.sleep(delayMillis)
            inventoryClick(windowId, slotTo, ClickType.PICKUP)
            Thread.sleep(delayMillis)
            inventoryClick(windowId, slotFrom, ClickType.PICKUP)
            if (prevScreen !is GuiContainer) mc.displayGuiScreen(prevScreen)
            inProgress = false
        }).start()
    }

    /**
     * Move all the item that equals to the item in [slotFrom] to [slotTo],
     * if [slotTo] contains an item, then move it to [slotFrom]
     * Note: Not working
     */
    fun moveAllToSlot(slotFrom: Int, slotTo: Int, delayMillis: Long) {
        if (inProgress) return
        Thread(Runnable {
            inProgress = true
            val prevScreen = mc.currentScreen
            mc.displayGuiScreen(GuiInventory(mc.player))
            Thread.sleep(delayMillis)
            inventoryClick(slotTo, ClickType.PICKUP_ALL)
            Thread.sleep(delayMillis)
            inventoryClick(slotTo, ClickType.PICKUP)
            mc.displayGuiScreen(prevScreen)
            inProgress = false
        }).start()
    }

    /**
     * Quick move (Shift + Click) the item in [slotFrom] in player inventory
     */
    fun quickMoveSlot(slotFrom: Int, delayMillis: Long) {
        quickMoveSlot(mc.player.inventoryContainer.windowId, slotFrom, delayMillis)
    }

    /**
     * Quick move (Shift + Click) the item in [slotFrom] in specified [windowID]
     */
    fun quickMoveSlot(windowID: Int, slotFrom: Int, delayMillis: Long) {
        if (inProgress) return
        Thread(Runnable {
            inProgress = true
            inventoryClick(windowID, slotFrom, ClickType.QUICK_MOVE)
            Thread.sleep(delayMillis)
            inProgress = false
        }).start()
    }

    /**
     * Throw all the item in [slot] in player inventory
     */
    fun throwAllInSlot(slot: Int, delayMillis: Long) {
        throwAllInSlot(mc.player.inventoryContainer.windowId, slot, delayMillis)
    }

    /**
     * Throw all the item in [slot] in specified [windowID]
     */
    fun throwAllInSlot(windowID: Int, slot: Int, delayMillis: Long) {
        if (inProgress) return
        Thread(Runnable {
            inProgress = true
            mc.playerController.windowClick(windowID, slot, 1, ClickType.THROW, mc.player)
            Thread.sleep(delayMillis)
            inProgress = false
        }).start()
    }
    /* End of inventory management */
}