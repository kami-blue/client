package me.zeroeightsix.kami.mixin.client.accessor

import net.minecraft.item.ItemTool

val ItemTool.attackDamage get() = (this as AccessorItemTool).attackDamage