package org.kamiblue.client.util.items

import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Container
import net.minecraft.inventory.Slot
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import java.util.function.Predicate

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

fun Iterable<Slot>.hasEmpty() =
    any {
        it.stack.isEmpty
    }

fun Iterable<Slot>.countEmpty() =
    count { it.stack.isEmpty }

inline fun <reified B : Block> Iterable<Slot>.countBlock(predicate: Predicate<ItemStack>? = null) =
    countByStack { itemStack ->
        itemStack.item.let { it is ItemBlock && it.block is B } && (predicate == null || predicate.test(itemStack))
    }

fun Iterable<Slot>.countBlock(block: Block, predicate: Predicate<ItemStack>? = null) =
    countByStack { itemStack ->
        itemStack.item.let { it is ItemBlock && it.block == block } && (predicate == null || predicate.test(itemStack))
    }

inline fun <reified I : Item> Iterable<Slot>.countItem(predicate: Predicate<ItemStack>? = null) =
    countByStack { it.item is I && (predicate == null || predicate.test(it)) }

fun Iterable<Slot>.countItem(item: Item, predicate: Predicate<ItemStack>? = null) =
    countByStack { it.item == item && (predicate == null || predicate.test(it)) }

fun Iterable<Slot>.countID(itemID: Int, predicate: Predicate<ItemStack>? = null) =
    countByStack { it.item.id == itemID && (predicate == null || predicate.test(it)) }

fun Iterable<Slot>.countByStack(predicate: Predicate<ItemStack>? = null) =
    sumBy { slot ->
        slot.stack.let { if (predicate == null || predicate.test(it)) it.count else 0 }
    }


fun <T : Slot> Iterable<T>.firstEmpty() =
    firstByStack { it.isEmpty }

inline fun <reified B : Block, T : Slot> Iterable<T>.firstBlock(predicate: Predicate<ItemStack>? = null) =
    firstByStack { itemStack ->
        itemStack.item.let { it is ItemBlock && it.block is B } && (predicate == null || predicate.test(itemStack))
    }

fun <T : Slot> Iterable<T>.firstBlock(block: Block, predicate: Predicate<ItemStack>? = null) =
    firstByStack { itemStack ->
        itemStack.item.let { it is ItemBlock && it.block == block } && (predicate == null || predicate.test(itemStack))
    }

inline fun <reified I : Item, T : Slot> Iterable<T>.firstItem(predicate: Predicate<ItemStack>? = null) =
    firstByStack {
        it.item is I && (predicate == null || predicate.test(it))
    }

fun <T : Slot> Iterable<T>.firstItem(item: Item, predicate: Predicate<ItemStack>? = null) =
    firstByStack {
        it.item == item && (predicate == null || predicate.test(it))
    }

fun <T : Slot> Iterable<T>.firstID(itemID: Int, predicate: Predicate<ItemStack>? = null) =
    firstByStack {
        it.item.id == itemID && (predicate == null || predicate.test(it))
    }

fun <T : Slot> Iterable<T>.firstByStack(predicate: Predicate<ItemStack>? = null): T? =
    firstOrNull { (predicate == null || predicate.test(it.stack)) }


inline fun <reified B : Block, T : Slot> Iterable<T>.forEmpty() =
    filterByStack { it.isEmpty }

inline fun <reified B : Block, T : Slot> Iterable<T>.filterByBlock(predicate: Predicate<ItemStack>? = null) =
    filterByStack { itemStack ->
        itemStack.item.let { it is ItemBlock && it.block is B } && (predicate == null || predicate.test(itemStack))
    }

fun <T : Slot> Iterable<T>.filterByBlock(block: Block, predicate: Predicate<ItemStack>? = null) =
    filterByStack { itemStack ->
        itemStack.item.let { it is ItemBlock && it.block == block } && (predicate == null || predicate.test(itemStack))
    }

inline fun <reified I : Item, T : Slot> Iterable<T>.filterByItem(predicate: Predicate<ItemStack>? = null) =
    filterByStack {
        it.item is I && (predicate == null || predicate.test(it))
    }

fun <T : Slot> Iterable<T>.filterByItem(item: Item, predicate: Predicate<ItemStack>? = null) =
    filterByStack {
        it.item == item && (predicate == null || predicate.test(it))
    }

fun <T : Slot> Iterable<T>.filterByID(itemID: Int, predicate: Predicate<ItemStack>? = null) =
    filterByStack { it.item.id == itemID && (predicate == null || predicate.test(it)) }

fun <T : Slot> Iterable<T>.filterByStack(predicate: Predicate<ItemStack>? = null) =
    filter { predicate == null || predicate.test(it.stack) }


class HotbarSlot(slot: Slot) : Slot(slot.inventory, slot.slotIndex, slot.xPos, slot.yPos) {
    init {
        slotNumber = slot.slotNumber
    }

    val hotbarSlot = slot.slotNumber - 36
}