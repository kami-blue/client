package org.kamiblue.client.util.inventory.slot

import net.minecraft.block.Block
import net.minecraft.inventory.Slot
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import org.kamiblue.client.util.items.id

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
    countByStack {
        it.item is I && predicate(it)
    }

fun Iterable<Slot>.countItem(item: Item, predicate: (ItemStack) -> Boolean = { true }) =
    countByStack {
        it.item == item && predicate(it)
    }

fun Iterable<Slot>.countID(itemID: Int, predicate: (ItemStack) -> Boolean = { true }) =
    countByStack {
        it.item.id == itemID && predicate(it)
    }

inline fun Iterable<Slot>.countByStack(predicate: (ItemStack) -> Boolean = { true }) =
    sumBy { slot ->
        slot.stack.let { if (predicate(it)) it.count else 0 }
    }


fun <T : Slot> Iterable<T>.firstEmpty() =
    firstByStack {
        it.isEmpty
    }

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
    firstOrNull {
        predicate(it.stack)
    }


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
    filterByStack {
        it.item.id == itemID && predicate(it)
    }

inline fun <T : Slot> Iterable<T>.filterByStack(predicate: (ItemStack) -> Boolean = { true }) =
    filter {
        predicate(it.stack)
    }