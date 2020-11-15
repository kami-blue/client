package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.mixin.client.player.MixinEntityPlayerSP
import me.zeroeightsix.kami.module.Module

/**
 * @see MixinEntityPlayerSP
 */
@Module.Info(
        name = "PortalChat",
        category = Module.Category.CHAT,
        description = "Allows you to open GUIs in portals",
        showOnArray = Module.ShowOnArray.OFF
)
object PortalChat : Module()
