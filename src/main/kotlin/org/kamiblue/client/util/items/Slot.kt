package org.kamiblue.client.util.items

import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Container
import net.minecraft.inventory.Slot
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import org.kamiblue.client.util.Wrapper

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

inline fun <reified B : Block> Iterable<Slot>.countBlock(predicate: (ItemStack) -> Boolean = { true }) =
    countByStack { itemStack ->
        itemStack.item.let { it is ItemBlock && it.block is B } && predicate(itemStack)
    }

fun Iterable<Slot>.countBlock(block: Block, predicate: (ItemStack) -> Boolean = { true }) =
    countByStack { itemStack ->
        itemStack.item.let { it is ItemBlock && it.block == block } && predicate(itemStack)
    }

inline fun <reified I : Item> Iterable<Slot>.countItem(predicate: (ItemStack) -> Boolean = { true }) =
    countByStack { it.item is I && predicate(it) }

fun Iterable<Slot>.countItem(item: Item, predicate: (ItemStack) -> Boolean = { true }) =
    countByStack { it.item == item && predicate(it) }

fun Iterable<Slot>.countID(itemID: Int, predicate: (ItemStack) -> Boolean = { true }) =
    countByStack { it.item.id == itemID && predicate(it) }

inline fun Iterable<Slot>.countByStack(predicate: (ItemStack) -> Boolean = { true }) =
    sumBy { slot ->
        slot.stack.let { if (predicate(it)) it.count else 0 }
    }


fun <T : Slot> Iterable<T>.firstEmpty() =
    firstByStack { it.isEmpty }

inline fun <reified B : Block, T : Slot> Iterable<T>.firstBlock(predicate: (ItemStack) -> Boolean = { true }) =
    firstByStack { itemStack ->
        itemStack.item.let { it is ItemBlock && it.block is B } && predicate(itemStack)
    }

fun <T : Slot> Iterable<T>.firstBlock(block: Block, predicate: (ItemStack) -> Boolean = { true }) =
    firstByStack { itemStack ->
        itemStack.item.let { it is ItemBlock && it.block == block } && predicate(itemStack)
    }

inline fun <reified I : Item, T : Slot> Iterable<T>.firstItem(predicate: (ItemStack) -> Boolean = { true }) =
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

inline fun <T : Slot> Iterable<T>.firstByStack(predicate: (ItemStack) -> Boolean): T? =
    firstOrNull { predicate(it.stack) }


inline fun <reified B : Block, T : Slot> Iterable<T>.forEmpty() =
    filterByStack { it.isEmpty }

inline fun <reified B : Block, T : Slot> Iterable<T>.filterByBlock(predicate: (ItemStack) -> Boolean = { true }) =
    filterByStack { itemStack ->
        itemStack.item.let { it is ItemBlock && it.block is B } && predicate(itemStack)
    }

fun <T : Slot> Iterable<T>.filterByBlock(block: Block, predicate: (ItemStack) -> Boolean = { true }) =
    filterByStack { itemStack ->
        itemStack.item.let { it is ItemBlock && it.block == block } && predicate(itemStack)
    }

inline fun <reified I : Item, T : Slot> Iterable<T>.filterByItem(predicate: (ItemStack) -> Boolean = { true }) =
    filterByStack {
        it.item is I && predicate(it)
    }

fun <T : Slot> Iterable<T>.filterByItem(item: Item, predicate: (ItemStack) -> Boolean = { true }) =
    filterByStack {
        it.item == item && predicate(it)
    }

fun <T : Slot> Iterable<T>.filterByID(itemID: Int, predicate: (ItemStack) -> Boolean = { true }) =
    filterByStack { it.item.id == itemID && predicate(it) }

inline fun <T : Slot> Iterable<T>.filterByStack(predicate: (ItemStack) -> Boolean = { true }) =
    filter { predicate(it.stack) }

fun Slot.toHotbarSlotOrNull() =
    if (this.slotNumber in 36..44 && this.inventory == Wrapper.player?.inventory) HotbarSlot(this)
    else null

class HotbarSlot(slot: Slot) : Slot(slot.inventory, slot.slotIndex, slot.xPos, slot.yPos) {
    init {
        slotNumber = slot.slotNumber
    }

    val hotbarSlot = slot.slotNumber - 36
}