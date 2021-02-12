package org.kamiblue.client.module.modules.player

import net.minecraft.item.ItemPickaxe
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module

internal object NoEntityTrace : Module(
    name = "NoEntityTrace",
    category = Category.PLAYER,
    description = "Blocks entities from stopping you from mining"
) {
    private val sneakTrigger = setting("Sneak Trigger", false)
    private val pickaxeOnly = setting("Pickaxe Only", true)

    fun shouldIgnoreEntity() = isEnabled && (!sneakTrigger.value || mc.player?.isSneaking == true)
        && (!pickaxeOnly.value || mc.player?.heldItemMainhand?.item is ItemPickaxe)
}