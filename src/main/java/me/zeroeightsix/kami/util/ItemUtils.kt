package me.zeroeightsix.kami.util

import net.minecraft.item.Item
import net.minecraft.item.ItemAxe
import net.minecraft.item.ItemSword

val Item.id get() = Item.getIdFromItem(this)

val Item.isWeapon get() = this is ItemSword || this is ItemAxe