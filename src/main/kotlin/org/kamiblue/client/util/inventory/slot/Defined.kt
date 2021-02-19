package org.kamiblue.client.util.inventory.slot

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Container
import net.minecraft.inventory.Slot

val EntityPlayer.allSlots: List<Slot>
    get() = inventoryContainer.getSlots(1..45)

val EntityPlayer.armorSlots: List<Slot>
    get() = inventoryContainer.getSlots(5..8)

val EntityPlayer.headSlot: Slot
    get() = inventoryContainer.inventorySlots[5]

val EntityPlayer.chestSlot: Slot
    get() = inventoryContainer.inventorySlots[6]

val EntityPlayer.legsSlot: Slot
    get() = inventoryContainer.inventorySlots[7]

val EntityPlayer.feetSlot: Slot
    get() = inventoryContainer.inventorySlots[8]

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

val EntityPlayer.currentHotbarSlot: HotbarSlot
    get() = HotbarSlot(inventoryContainer.getSlot(inventory.currentItem + 36))

val EntityPlayer.firstHotbarSlot: HotbarSlot
    get() = HotbarSlot(inventoryContainer.getSlot(36))

fun Container.getSlots(range: IntRange): List<Slot> =
    inventorySlots.subList(range.first, range.last + 1)

