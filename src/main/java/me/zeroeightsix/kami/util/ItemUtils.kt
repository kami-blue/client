package me.zeroeightsix.kami.util

import net.minecraft.block.Block
import net.minecraft.item.*

val ItemStack.originalName: String get() = item.getItemStackDisplayName(this)

val Item.id get() = Item.getIdFromItem(this)

val Item.block: Block get() = Block.getBlockFromItem(this)

val Item.isWeapon get() = this is ItemSword || this is ItemAxe

val ItemFood.foodValue get() = this.getHealAmount(ItemStack.EMPTY)

val ItemFood.saturation get() = foodValue * this.getSaturationModifier(ItemStack.EMPTY) * 2f