package me.zeroeightsix.kami.util

import net.minecraft.block.Block
import net.minecraft.item.Item

val Block.item: Item get() = Item.getItemFromBlock(this)

val Block.id: Int get() = Block.getIdFromBlock(this)