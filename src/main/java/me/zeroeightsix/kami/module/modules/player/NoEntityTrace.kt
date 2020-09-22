package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.item.ItemPickaxe

@Module.Info(
        name = "NoEntityTrace",
        category = Module.Category.PLAYER,
        description = "Blocks entities from stopping you from mining"
)
object NoEntityTrace : Module() {
    private val sneakTrigger = register(Settings.b("SneakTrigger", false))
    private val pickaxeOnly = register(Settings.b("PickaxeOnly", true))

    fun shouldIgnoreEntity() = isEnabled && (!sneakTrigger.value || mc.player?.isSneaking == true)
            && (!pickaxeOnly.value || mc.player?.heldItemMainhand?.getItem() is ItemPickaxe)
}