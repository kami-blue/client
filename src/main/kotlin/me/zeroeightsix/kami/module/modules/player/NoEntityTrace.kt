package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import net.minecraft.item.ItemPickaxe

internal object NoEntityTrace : Module(
    name = "NoEntityTrace",
    category = Category.PLAYER,
    description = "Blocks entities from stopping you from mining"
) {
    private val sneakTrigger = setting("SneakTrigger", false)
    private val pickaxeOnly = setting("PickaxeOnly", true)

    fun shouldIgnoreEntity() = isEnabled && (!sneakTrigger.value || mc.player?.isSneaking == true)
        && (!pickaxeOnly.value || mc.player?.heldItemMainhand?.item is ItemPickaxe)
}