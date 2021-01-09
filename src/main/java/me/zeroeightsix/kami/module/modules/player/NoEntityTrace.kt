package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import net.minecraft.item.ItemPickaxe

object NoEntityTrace : Module(
    category = Category.PLAYER,
) {
    private val sneakTrigger = setting(getTranslationKey("SneakTrigger"), false)
    private val pickaxeOnly = setting(getTranslationKey("PickaxeOnly"), true)

    fun shouldIgnoreEntity() = isEnabled && (!sneakTrigger.value || mc.player?.isSneaking == true)
        && (!pickaxeOnly.value || mc.player?.heldItemMainhand?.item is ItemPickaxe)
}