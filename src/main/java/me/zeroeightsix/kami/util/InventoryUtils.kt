package me.zeroeightsix.kami.util

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.inventory.ClickType
import net.minecraft.item.Item.getIdFromItem

object InventoryUtils {
    private val mc = Minecraft.getMinecraft()

    /**
     * Returns slots contains item with given item id  in full player inventory
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

    /**
     * Returns slots in full inventory contains item with given [itemId] in full player inventory
     * This is same as [getSlots] but it returns full inventory slot index
     *
     * @return Array contains full inventory slot index, null if no item found
     */
    fun getSlotsFullInv(min: Int, max: Int, itemID: Int): Array<Int>? {
        val slots = arrayListOf<Int>()
        for (i in min..max) {
            if (getIdFromItem(mc.player.inventoryContainer.inventory[i].getItem()) == itemID) {
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
        return getSlots(36, 44, itemId)
    }

    /**
     * Returns slots contains with given [itemId] in player inventory (without hotbar)
     * This is same as [getSlots] but it returns full inventory slot index
     *
     * @return Array contains slot index, null if no item found
     */
    fun getSlotsFullInvNoHotbar(itemId: Int): Array<Int>? {
        return getSlots(9, 35, itemId)
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
            if (getIdFromItem(currentItemStack.getItem()) != exceptionID) {
                slot2 = i
                break
            }
        }
        for (i in 36..44) { /* If an empty slot is found, then overwrite it */
            val currentItemStack = mc.player.inventoryContainer.inventory[i]
            if (currentItemStack.isEmpty) {
                slot2 = i
                break
            }
        }
        moveToSlot(slot1, slot2)
        mc.playerController.syncCurrentPlayItem()
    }

    /**
     * Move the item in [slotFrom] to [slotTo], if [slotTo] contains an item, then move it to [slotFrom]
     */
    fun moveToSlot(slotFrom: Int, slotTo: Int) {
        val prevScreen = mc.currentScreen
        if (mc.currentScreen !is GuiInventory && !mc.player.isCreative) mc.displayGuiScreen(GuiInventory(mc.player))
        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slotFrom, 0, ClickType.PICKUP, mc.player)
        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slotTo, 0, ClickType.PICKUP, mc.player)
        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slotFrom, 0, ClickType.PICKUP, mc.player)
        mc.displayGuiScreen(prevScreen)
        mc.playerController.syncCurrentPlayItem()
    }

    /**
     * Move all items equals to the item in [slotFrom] to [slotTo], if [slotTo] contains an item, then move it to [slotFrom]
     */
    fun moveAllToSlot(slotFrom: Int, slotTo: Int) {
        val prevScreen = mc.currentScreen
        if (mc.currentScreen !is GuiInventory && !mc.player.isCreative) mc.displayGuiScreen(GuiInventory(mc.player))
        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slotFrom, 0, ClickType.PICKUP_ALL, mc.player)
        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slotTo, 0, ClickType.PICKUP, mc.player)
        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slotFrom, 0, ClickType.PICKUP, mc.player)
        mc.displayGuiScreen(prevScreen)
        mc.playerController.syncCurrentPlayItem()
    }

    /**
     * Quick move the item in [slotFrom] (Shift + Click)
     */
    fun quickMoveSlot(slotFrom: Int) {
        val prevScreen = mc.currentScreen
        if (mc.currentScreen !is GuiInventory && !mc.player.isCreative) mc.displayGuiScreen(GuiInventory(mc.player))
        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slotFrom, 0, ClickType.QUICK_MOVE, mc.player)
        mc.displayGuiScreen(prevScreen)
        mc.playerController.syncCurrentPlayItem()
    }
    /* End of inventory management */
}